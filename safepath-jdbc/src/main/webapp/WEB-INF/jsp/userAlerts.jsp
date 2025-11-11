<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <title>User Alerts Management</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        table { border-collapse: collapse; width: 100%; margin-top: 20px; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #4CAF50; color: white; }
        tr:nth-child(even) { background-color: #f2f2f2; }
        .form-section { margin: 20px 0; padding: 15px; border: 1px solid #ccc; background-color: #f9f9f9; }
        .form-section h3 { margin-top: 0; }
        input[type="text"], input[type="number"], input[type="checkbox"], select { 
            width: 200px; padding: 5px; margin: 5px 0; 
        }
        button { padding: 8px 15px; margin: 5px; cursor: pointer; }
        .btn-create { background-color: #4CAF50; color: white; border: none; }
        .btn-update { background-color: #2196F3; color: white; border: none; }
        .btn-delete { background-color: #f44336; color: white; border: none; }
        .btn-edit { background-color: #FF9800; color: white; border: none; }
        .btn-search { background-color: #9C27B0; color: white; border: none; }
        .btn-clear { background-color: #607D8B; color: white; border: none; }
        .actions { display: flex; gap: 5px; }
        .form-row { display: flex; flex-wrap: wrap; gap: 10px; margin: 10px 0; }
        .form-row label { display: flex; flex-direction: column; }
        .search-section { margin: 20px 0; padding: 15px; border: 1px solid #4CAF50; background-color: #e8f5e9; }
    </style>
</head>
<body>
<h2>User Alerts Management - CRUD Operations</h2>

<!-- Search Form -->
<div class="search-section">
    <h3>Search User Alerts</h3>
    <form method="GET" action="${pageContext.request.contextPath}/alerts">
        <div class="form-row">
            <label>User ID: <input type="number" name="searchUserId" value="${searchUserId != null ? searchUserId : ''}" placeholder="User ID"></label>
            <label>Alert ID: <input type="number" name="searchAlertId" value="${searchAlertId != null ? searchAlertId : ''}" placeholder="Alert ID"></label>
        </div>
        <button type="submit" class="btn-search">Search</button>
        <a href="${pageContext.request.contextPath}/alerts"><button type="button" class="btn-clear">Clear</button></a>
    </form>
</div>

<!-- Create Form -->
<div class="form-section">
    <h3>Create New User Alert</h3>
    <form method="POST" action="${pageContext.request.contextPath}/alerts">
        <input type="hidden" name="action" value="create">
        <div class="form-row">
            <label>User ID: <input type="number" name="userId" required></label>
            <label>Radius (meters): <input type="number" name="radiusM"></label>
            <label>Center Latitude: <input type="number" step="0.000001" name="centerLat"></label>
            <label>Center Longitude: <input type="number" step="0.000001" name="centerLon"></label>
            <label>Crime Type Filter: <input type="text" name="crimeTypeFilter"></label>
            <label>Active: 
                <select name="activeFlag">
                    <option value="">-- Select --</option>
                    <option value="true">True</option>
                    <option value="false">False</option>
                </select>
            </label>
        </div>
        <button type="submit" class="btn-create">Create Alert</button>
    </form>
</div>

<!-- Update Form (initially hidden, shown when editing) -->
<div class="form-section" id="updateForm" style="display: none;">
    <h3>Edit User Alert</h3>
    <form method="POST" action="${pageContext.request.contextPath}/alerts" id="editForm">
        <input type="hidden" name="action" value="update">
        <input type="hidden" name="id" id="editAlertId">
        <div class="form-row">
            <label>Alert ID: <input type="text" id="editAlertIdDisplay" readonly style="background-color: #e0e0e0;"></label>
            <label>User ID: <input type="number" name="userId" id="editUserId" required></label>
            <label>Radius (meters): <input type="number" name="radiusM" id="editRadiusM"></label>
            <label>Center Latitude: <input type="number" step="0.000001" name="centerLat" id="editCenterLat"></label>
            <label>Center Longitude: <input type="number" step="0.000001" name="centerLon" id="editCenterLon"></label>
            <label>Crime Type Filter: <input type="text" name="crimeTypeFilter" id="editCrimeTypeFilter"></label>
            <label>Active: 
                <select name="activeFlag" id="editActiveFlag">
                    <option value="">-- Select --</option>
                    <option value="true">True</option>
                    <option value="false">False</option>
                </select>
            </label>
        </div>
        <button type="submit" class="btn-update">Update Alert</button>
        <button type="button" onclick="cancelEdit()">Cancel</button>
    </form>
</div>

<!-- User Alerts List -->
<h3>User Alerts List</h3>
<table>
    <tr>
        <th>Alert ID</th>
        <th>User ID</th>
        <th>Radius(m)</th>
        <th>Center Lat</th>
        <th>Center Lon</th>
        <th>Crime Type Filter</th>
        <th>Active</th>
        <th>Created</th>
        <th>Updated</th>
        <th>Actions</th>
    </tr>
    <c:forEach var="a" items="${alerts}">
        <tr data-alert-id="${a.alertId}"
            data-user-id="${a.userId != null ? a.userId : ''}"
            data-radius-m="${a.radiusM != null ? a.radiusM : ''}"
            data-center-lat="${a.centerLat != null ? a.centerLat : ''}"
            data-center-lon="${a.centerLon != null ? a.centerLon : ''}"
            data-crime-type-filter="${a.crimeTypeFilter != null ? a.crimeTypeFilter : ''}"
            data-active-flag="${a.activeFlag != null ? a.activeFlag : ''}">
            <td>${a.alertId}</td>
            <td>${a.userId}</td>
            <td>${a.radiusM}</td>
            <td>${a.centerLat}</td>
            <td>${a.centerLon}</td>
            <td>${a.crimeTypeFilter}</td>
            <td>${a.activeFlag}</td>
            <td>${a.createdAt}</td>
            <td>${a.updatedAt}</td>
            <td class="actions">
                <button class="btn-edit" onclick="editAlertFromRow(this)">Edit</button>
                <button class="btn-delete" onclick="deleteAlert('${a.alertId}')">Delete</button>
            </td>
        </tr>
    </c:forEach>
</table>

<script>
function editAlertFromRow(button) {
    var row = button.closest('tr');
    var alertId = row.getAttribute('data-alert-id');
    var userId = row.getAttribute('data-user-id') || '';
    var radiusM = row.getAttribute('data-radius-m') || '';
    var centerLat = row.getAttribute('data-center-lat') || '';
    var centerLon = row.getAttribute('data-center-lon') || '';
    var crimeTypeFilter = row.getAttribute('data-crime-type-filter') || '';
    var activeFlag = row.getAttribute('data-active-flag') || '';
    
    document.getElementById('editAlertId').value = alertId;
    document.getElementById('editAlertIdDisplay').value = alertId;
    document.getElementById('editUserId').value = userId;
    document.getElementById('editRadiusM').value = radiusM;
    document.getElementById('editCenterLat').value = centerLat;
    document.getElementById('editCenterLon').value = centerLon;
    document.getElementById('editCrimeTypeFilter').value = crimeTypeFilter;
    document.getElementById('editActiveFlag').value = activeFlag;
    document.getElementById('updateForm').style.display = 'block';
    document.getElementById('updateForm').scrollIntoView({ behavior: 'smooth' });
}

function cancelEdit() {
    document.getElementById('updateForm').style.display = 'none';
    document.getElementById('editForm').reset();
}

function deleteAlert(alertId) {
    if (confirm('Are you sure you want to delete alert ID ' + alertId + '?')) {
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '${pageContext.request.contextPath}/alerts';
        
        const actionInput = document.createElement('input');
        actionInput.type = 'hidden';
        actionInput.name = 'action';
        actionInput.value = 'delete';
        form.appendChild(actionInput);
        
        const idInput = document.createElement('input');
        idInput.type = 'hidden';
        idInput.name = 'id';
        idInput.value = parseInt(alertId);
        form.appendChild(idInput);
        
        document.body.appendChild(form);
        form.submit();
    }
}
</script>

<p><a href="${pageContext.request.contextPath}/">Back to Home</a></p>
</body>
</html>

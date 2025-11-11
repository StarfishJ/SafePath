<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <title>Crime Reports Management</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        table { border-collapse: collapse; width: 100%; margin-top: 20px; font-size: 12px; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #4CAF50; color: white; }
        tr:nth-child(even) { background-color: #f2f2f2; }
        .form-section { margin: 20px 0; padding: 15px; border: 1px solid #ccc; background-color: #f9f9f9; }
        .form-section h3 { margin-top: 0; }
        input[type="text"], input[type="datetime-local"], input[type="number"] { 
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
<h2>Crime Reports Management - CRUD Operations</h2>

<!-- Search Form -->
<div class="search-section">
    <h3>Search Crime Reports</h3>
    <form method="GET" action="${pageContext.request.contextPath}/crimeReports">
        <div class="form-row">
            <label>Report Number: <input type="text" name="searchReportNumber" value="${searchReportNumber != null ? searchReportNumber : ''}" placeholder="Report number"></label>
            <label>Precinct: <input type="text" name="searchPrecinct" value="${searchPrecinct != null ? searchPrecinct : ''}" placeholder="Precinct"></label>
            <label>Neighborhood: <input type="text" name="searchNeighborhood" value="${searchNeighborhood != null ? searchNeighborhood : ''}" placeholder="Neighborhood"></label>
        </div>
        <button type="submit" class="btn-search">Search</button>
        <a href="${pageContext.request.contextPath}/crimeReports"><button type="button" class="btn-clear">Clear</button></a>
    </form>
</div>

<!-- Create Form -->
<div class="form-section">
    <h3>Create New Crime Report</h3>
    <form method="POST" action="${pageContext.request.contextPath}/crimeReports">
        <input type="hidden" name="action" value="create">
        <div class="form-row">
            <label>Report Number: <input type="text" name="reportNumber" required></label>
            <label>Report Datetime: <input type="datetime-local" name="reportDatetime" id="createDatetime"></label>
            <label>Precinct: <input type="text" name="precinct"></label>
            <label>Sector: <input type="text" name="sector"></label>
            <label>Beat: <input type="text" name="beat"></label>
            <label>Neighborhood: <input type="text" name="mcppNeighborhood"></label>
            <label>Address: <input type="text" name="blurredAddress"></label>
            <label>Latitude: <input type="number" step="0.000001" name="blurredLatitude"></label>
            <label>Longitude: <input type="number" step="0.000001" name="blurredLongitude"></label>
        </div>
        <button type="submit" class="btn-create">Create Report</button>
    </form>
</div>

<!-- Update Form (initially hidden, shown when editing) -->
<div class="form-section" id="updateForm" style="display: none;">
    <h3>Edit Crime Report</h3>
    <form method="POST" action="${pageContext.request.contextPath}/crimeReports" id="editForm">
        <input type="hidden" name="action" value="update">
        <input type="hidden" name="reportNumber" id="editReportNumber">
        <div class="form-row">
            <label>Report Number: <input type="text" id="editReportNumberDisplay" readonly style="background-color: #e0e0e0;"></label>
            <label>Report Datetime: <input type="datetime-local" name="reportDatetime" id="editDatetime"></label>
            <label>Precinct: <input type="text" name="precinct" id="editPrecinct"></label>
            <label>Sector: <input type="text" name="sector" id="editSector"></label>
            <label>Beat: <input type="text" name="beat" id="editBeat"></label>
            <label>Neighborhood: <input type="text" name="mcppNeighborhood" id="editNeighborhood"></label>
            <label>Address: <input type="text" name="blurredAddress" id="editAddress"></label>
            <label>Latitude: <input type="number" step="0.000001" name="blurredLatitude" id="editLatitude"></label>
            <label>Longitude: <input type="number" step="0.000001" name="blurredLongitude" id="editLongitude"></label>
        </div>
        <button type="submit" class="btn-update">Update Report</button>
        <button type="button" onclick="cancelEdit()">Cancel</button>
    </form>
</div>

<!-- Crime Reports List -->
<h3>Crime Reports List</h3>
<table>
    <tr>
        <th>Report #</th>
        <th>Datetime</th>
        <th>Precinct</th>
        <th>Sector</th>
        <th>Beat</th>
        <th>Neighborhood</th>
        <th>Address</th>
        <th>Lat</th>
        <th>Lon</th>
        <th>Actions</th>
    </tr>
    <c:forEach var="r" items="${reports}">
        <tr data-report-number="${r.reportNumber}"
            data-datetime="${r.reportDatetime != null ? r.reportDatetime.toString() : ''}"
            data-precinct="${r.precinct != null ? r.precinct : ''}"
            data-sector="${r.sector != null ? r.sector : ''}"
            data-beat="${r.beat != null ? r.beat : ''}"
            data-neighborhood="${r.mcppNeighborhood != null ? r.mcppNeighborhood : ''}"
            data-address="${r.blurredAddress != null ? r.blurredAddress : ''}"
            data-latitude="${r.blurredLatitude != null ? r.blurredLatitude : ''}"
            data-longitude="${r.blurredLongitude != null ? r.blurredLongitude : ''}">
            <td>${r.reportNumber}</td>
            <td>
                <c:if test="${r.reportDatetime != null}">
                    ${r.reportDatetime}
                </c:if>
            </td>
            <td>${r.precinct}</td>
            <td>${r.sector}</td>
            <td>${r.beat}</td>
            <td>${r.mcppNeighborhood}</td>
            <td>${r.blurredAddress}</td>
            <td>${r.blurredLatitude}</td>
            <td>${r.blurredLongitude}</td>
            <td class="actions">
                <button class="btn-edit" onclick="editReportFromRow(this)">Edit</button>
                <button class="btn-delete" onclick="deleteReport('${r.reportNumber}')">Delete</button>
            </td>
        </tr>
    </c:forEach>
</table>

<script>
function formatDateTimeForInput(dateTimeString) {
    if (!dateTimeString) return '';
    // Convert "2024-01-01T12:00:00" or "2024-01-01 12:00:00" to "2024-01-01T12:00"
    var formatted = dateTimeString.toString().replace(' ', 'T');
    // Remove seconds and milliseconds if present
    if (formatted.indexOf('.') > 0) {
        formatted = formatted.substring(0, formatted.indexOf('.'));
    }
    if (formatted.length > 16) {
        formatted = formatted.substring(0, 16);
    }
    return formatted;
}

function editReportFromRow(button) {
    var row = button.closest('tr');
    var reportNumber = row.getAttribute('data-report-number');
    var datetime = row.getAttribute('data-datetime');
    var precinct = row.getAttribute('data-precinct');
    var sector = row.getAttribute('data-sector');
    var beat = row.getAttribute('data-beat');
    var neighborhood = row.getAttribute('data-neighborhood');
    var address = row.getAttribute('data-address');
    var latitude = row.getAttribute('data-latitude');
    var longitude = row.getAttribute('data-longitude');
    
    document.getElementById('editReportNumber').value = reportNumber;
    document.getElementById('editReportNumberDisplay').value = reportNumber;
    document.getElementById('editDatetime').value = formatDateTimeForInput(datetime);
    document.getElementById('editPrecinct').value = precinct;
    document.getElementById('editSector').value = sector;
    document.getElementById('editBeat').value = beat;
    document.getElementById('editNeighborhood').value = neighborhood;
    document.getElementById('editAddress').value = address;
    document.getElementById('editLatitude').value = latitude;
    document.getElementById('editLongitude').value = longitude;
    document.getElementById('updateForm').style.display = 'block';
    document.getElementById('updateForm').scrollIntoView({ behavior: 'smooth' });
}

function cancelEdit() {
    document.getElementById('updateForm').style.display = 'none';
    document.getElementById('editForm').reset();
}

function deleteReport(reportNumber) {
    if (confirm('Are you sure you want to delete report ' + reportNumber + '?')) {
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '${pageContext.request.contextPath}/crimeReports';
        
        const actionInput = document.createElement('input');
        actionInput.type = 'hidden';
        actionInput.name = 'action';
        actionInput.value = 'delete';
        form.appendChild(actionInput);
        
        const reportInput = document.createElement('input');
        reportInput.type = 'hidden';
        reportInput.name = 'reportNumber';
        reportInput.value = reportNumber;
        form.appendChild(reportInput);
        
        document.body.appendChild(form);
        form.submit();
    }
}
</script>

<p><a href="${pageContext.request.contextPath}/">Back to Home</a></p>
</body>
</html>

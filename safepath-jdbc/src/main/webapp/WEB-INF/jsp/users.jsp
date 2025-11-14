<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <title>Users Management</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        table { border-collapse: collapse; width: 100%; margin-top: 20px; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #4CAF50; color: white; }
        tr:nth-child(even) { background-color: #f2f2f2; }
        .form-section { margin: 20px 0; padding: 15px; border: 1px solid #ccc; background-color: #f9f9f9; }
        .form-section h3 { margin-top: 0; }
        input[type="text"], input[type="password"], input[type="email"] { 
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
        .search-section { margin: 20px 0; padding: 15px; border: 1px solid #4CAF50; background-color: #e8f5e9; }
    </style>
</head>
<body>
<h2>Users Management - CRUD Operations</h2>

<!-- Search Form -->
<div class="search-section">
    <h3>Search Users</h3>
    <form method="GET" action="${pageContext.request.contextPath}/users">
        <label>Email: <input type="text" name="searchEmail" id="searchEmailInput" value="${searchEmail != null ? searchEmail : ''}" placeholder="Enter email to search"></label>
        <button type="submit" class="btn-search">Search</button>
        <button type="button" class="btn-clear" onclick="window.location.href='${pageContext.request.contextPath}/users'">Clear</button>
    </form>
    <c:if test="${searchEmail != null && !searchEmail.isEmpty()}">
        <p style="margin-top: 10px; color: #666;">
            <strong>Search results for: "${searchEmail}"</strong> 
            <c:choose>
                <c:when test="${users != null && !empty users}">
                    - Found ${users.size()} user(s)
                </c:when>
                <c:otherwise>
                    - No users found
                </c:otherwise>
            </c:choose>
        </p>
    </c:if>
</div>

<!-- Create Form -->
<div class="form-section">
    <h3>Create New User</h3>
    <form method="POST" action="${pageContext.request.contextPath}/users">
        <input type="hidden" name="action" value="create">
        <label>Email: <input type="email" name="email" required></label><br>
        <label>Password: <input type="password" name="password" required></label><br>
        <button type="submit" class="btn-create">Create User</button>
    </form>
</div>

<!-- Update Form (initially hidden, shown when editing) -->
<div class="form-section" id="updateForm" style="display: none;">
    <h3>Edit User</h3>
    <form method="POST" action="${pageContext.request.contextPath}/users" id="editForm">
        <input type="hidden" name="action" value="update">
        <input type="hidden" name="id" id="editUserId">
        <label>Email: <input type="email" name="email" id="editEmail" required></label><br>
        <label>Password (leave blank to keep current): <input type="password" name="password" id="editPassword"></label><br>
        <button type="submit" class="btn-update">Update User</button>
        <button type="button" onclick="cancelEdit()">Cancel</button>
    </form>
</div>

<!-- Users List -->
<h3>Users List 
    <c:if test="${searchEmail != null && !searchEmail.isEmpty()}">
        <span style="font-size: 0.8em; color: #666;">(Search Results)</span>
    </c:if>
</h3>
<c:choose>
    <c:when test="${users != null && !empty users}">
        <table>
    <tr>
        <th>User ID</th>
        <th>Email</th>
        <th>Created At</th>
        <th>Actions</th>
    </tr>
    <c:forEach var="u" items="${users}">
        <tr>
            <td>${u.userId}</td>
            <td>${u.email}</td>
            <td>${u.createdAt}</td>
            <td class="actions">
                <button class="btn-edit" onclick="editUser(${u.userId}, '${u.email}')">Edit</button>
                <button class="btn-delete" onclick="deleteUser(${u.userId})">Delete</button>
            </td>
        </tr>
    </c:forEach>
        </table>
    </c:when>
    <c:otherwise>
        <p style="padding: 20px; color: #666; font-style: italic;">
            <c:choose>
                <c:when test="${searchEmail != null && !searchEmail.isEmpty()}">
                    No users found matching "${searchEmail}".
                </c:when>
                <c:otherwise>
                    No users found. Create a new user to get started.
                </c:otherwise>
            </c:choose>
        </p>
    </c:otherwise>
</c:choose>

<script>
function editUser(userId, email) {
    document.getElementById('editUserId').value = userId;
    document.getElementById('editEmail').value = email;
    document.getElementById('editPassword').value = '';
    document.getElementById('updateForm').style.display = 'block';
    document.getElementById('updateForm').scrollIntoView({ behavior: 'smooth' });
}

function cancelEdit() {
    document.getElementById('updateForm').style.display = 'none';
    document.getElementById('editForm').reset();
}

function deleteUser(userId) {
    if (confirm('Are you sure you want to delete user ID ' + userId + '?')) {
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '${pageContext.request.contextPath}/users';
        
        const actionInput = document.createElement('input');
        actionInput.type = 'hidden';
        actionInput.name = 'action';
        actionInput.value = 'delete';
        form.appendChild(actionInput);
        
        const idInput = document.createElement('input');
        idInput.type = 'hidden';
        idInput.name = 'id';
        idInput.value = userId;
        form.appendChild(idInput);
        
        document.body.appendChild(form);
        form.submit();
    }
}
</script>

<p><a href="${pageContext.request.contextPath}/">Back to Home</a></p>
</body>
</html>

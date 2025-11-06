<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head><title>Users</title></head>
<body>
<h2>Users</h2>
<table border="1">
    <tr><th>ID</th><th>Username</th><th>Email</th></tr>
    <c:forEach var="u" items="${users}">
        <tr>
            <td>${u.id}</td>
            <td>${u.username}</td>
            <td>${u.email}</td>
        </tr>
    </c:forEach>
</table>
<p>Use POST /users?action=create|update|delete with parameters to perform CRUD (see README)</p>
<p><a href="${pageContext.request.contextPath}/">Back</a></p>
</body>
</html>

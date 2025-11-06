<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head><title>User Alerts</title></head>
<body>
<h2>User Alerts</h2>
<table border="1">
    <tr><th>ID</th><th>User ID</th><th>Message</th></tr>
    <c:forEach var="a" items="${alerts}">
        <tr>
            <td>${a.id}</td>
            <td>${a.userId}</td>
            <td>${a.message}</td>
        </tr>
    </c:forEach>
</table>
<p>Use POST /alerts?action=create|update|delete with parameters to perform CRUD (see README)</p>
<p><a href="${pageContext.request.contextPath}/">Back</a></p>
</body>
</html>

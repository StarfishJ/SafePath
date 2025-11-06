<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head><title>Crime Reports</title></head>
<body>
<h2>Crime Reports</h2>
<table border="1">
    <tr><th>ID</th><th>Description</th><th>Location</th><th>Reported At</th></tr>
    <c:forEach var="r" items="${reports}">
        <tr>
            <td>${r.id}</td>
            <td>${r.description}</td>
            <td>${r.location}</td>
            <td>${r.reportedAt}</td>
        </tr>
    </c:forEach>
</table>
<p>Use POST /crimeReports?action=create|update|delete with parameters to perform CRUD (see README)</p>
<p><a href="${pageContext.request.contextPath}/">Back</a></p>
</body>
</html>

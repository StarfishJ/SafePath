<%@ page isErrorPage="true" contentType="text/html;charset=UTF-8" %>
<html>
<head><title>404 Not Found</title></head>
<body>
<h2>404 - Unavailable</h2>
<p>Request Path: ${pageContext.errorData.requestURI}</p>
<p>Status Code: 404</p>
<p><a href="${pageContext.request.contextPath}/">Back to Home</a></p>
</body>
</html>



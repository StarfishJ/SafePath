<%@ page isErrorPage="true" contentType="text/html;charset=UTF-8" %>
<html>
<head><title>500 Internal Server Error</title></head>
<body>
<h2>500 - Internal Server Error</h2>
<p>Request Path: ${pageContext.errorData.requestURI}</p>
<p>Status Code: 500</p>
<c:if test="${not empty exception}">
    <h3>Exception Information</h3>
    <pre><%= exception.getMessage() %></pre>
</c:if>
<p><a href="${pageContext.request.contextPath}/">Back to Home</a></p>
</body>
</html>



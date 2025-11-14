<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Safety Recommendations</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .form-section { margin: 20px 0; padding: 15px; border: 1px solid #ccc; background-color: #f9f9f9; }
        .form-row { display: flex; flex-wrap: wrap; gap: 10px; margin: 10px 0; }
        .form-row label { display: flex; flex-direction: column; }
        input[type="number"], input[type="text"] { width: 200px; padding: 5px; }
        button { padding: 8px 15px; margin: 5px; cursor: pointer; }
        table { border-collapse: collapse; width: 100%; margin-top: 20px; font-size: 12px; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #4CAF50; color: white; }
        .insight { padding: 12px; background: #e8f5e9; border: 1px solid #4CAF50; margin: 10px 0; }
    </style>
    <script>
    function useExample() {
        document.getElementById('centerLat').value = '47.6062';
        document.getElementById('centerLon').value = '-122.3321';
        document.getElementById('radiusM').value = '1000';
        document.getElementById('days').value = '30';
    }
    </script>
    </head>
<body>
<h2>Safety Recommendations (Demo)</h2>

<div class="form-section">
    <h3>Query Window</h3>
    <form method="GET" action="${pageContext.request.contextPath}/safety">
        <div class="form-row">
            <label>User ID (optional): <input type="number" name="userId" value="${userId != null ? userId : ''}" placeholder="Prefill from first alert"></label>
            <label>Center Latitude: <input type="text" id="centerLat" name="centerLat" value="${centerLat != null ? centerLat : ''}"></label>
            <label>Center Longitude: <input type="text" id="centerLon" name="centerLon" value="${centerLon != null ? centerLon : ''}"></label>
            <label>Radius (meters): <input type="number" id="radiusM" name="radiusM" value="${radiusM != null ? radiusM : '1000'}"></label>
            <label>Recent Days: <input type="number" id="days" name="days" value="${days != null ? days : '30'}"></label>
        </div>
        <button type="submit">Analyze</button>
        <button type="button" onclick="useExample()">Use Seattle Downtown Example</button>
        <a href="${pageContext.request.contextPath}/"><button type="button">Back</button></a>
    </form>
</div>

<c:if test="${totalCount != null}">
    <div class="insight">
        <strong>Total incidents found:</strong> ${totalCount}<br/>
        <c:if test="${not empty topNeighborhoods}">
            <strong>Top neighborhoods:</strong>
            <c:forEach var="e" items="${topNeighborhoods}" varStatus="st">
                <c:out value="${e.key}"/> (<c:out value="${e.value}"/>)<c:if test="${!st.last}">, </c:if>
            </c:forEach>
            <br/>
        </c:if>
        <strong>Recommendation:</strong> ${recommendation}
    </div>
</c:if>

<h3>Nearby Incidents</h3>
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
    </tr>
    <c:forEach var="r" items="${reports}">
        <tr>
            <td>${r.reportNumber}</td>
            <td>${r.reportDatetime}</td>
            <td>${r.precinct}</td>
            <td>${r.sector}</td>
            <td>${r.beat}</td>
            <td>${r.mcppNeighborhood}</td>
            <td>${r.blurredAddress}</td>
            <td>${r.blurredLatitude}</td>
            <td>${r.blurredLongitude}</td>
        </tr>
    </c:forEach>
</table>

<p><a href="${pageContext.request.contextPath}/">Back to Home</a></p>
</body>
</html>



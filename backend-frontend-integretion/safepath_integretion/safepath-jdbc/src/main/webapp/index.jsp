<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
    <head>
        <title>SafePath - Home</title>
        <link rel="stylesheet" href="style.css">
    </head>
    <body>
    <!-- Header -->
    <header class="header">
        <input type="text" id="location-input"  class="search-input" placeholder="Type your location .....">

        <div class="auth-buttons">
            <button>login</button>
            <button>register</button>
            <span class="logo">SafePath</span>
        </div>
    </header>

    <!-- Main layout -->
    <div class="main">
        <!-- Left side -->
        <div class="sidebar">

            <div class="filters">
                <button>distance</button>
                <button>type</button>
                <button>time</button>
            </div>

            <input type="text" class="search-destination" placeholder="where you want to go ......">
            <div id="route-details" class="route-details"></div>

            <!-- Alert items -->
            <div class="alert-list">
                <div class="alert-item">Alert event</div>
                <div class="alert-item">Alert event</div>
                <div class="alert-item">Alert event</div>
                <div class="alert-item">Alert event</div>
                <div class="alert-item">Alert event</div>
            </div>
        </div>

        <!-- Right side map -->
        <div class="map-area">
            <div id="map"></div>
        </div>
    </div>

    <!-- Floating button -->
    <button class="floating-btn">+</button>
    </body>


    <!-- 先加载 map.js 和 crime.js -->
    <script type="module" src="${pageContext.request.contextPath}/js/map.js"></script>
    <script type="module" src="${pageContext.request.contextPath}/js/crime.js"></script>

    <!-- Google Map 加载，手动初始化以确保模块已加载 -->
    <script>
        // 等待模块和 Google Maps API 都加载完成后初始化地图
        let mapsApiLoaded = false;
        let modulesLoaded = false;
        
        function tryInitMap() {
            if (mapsApiLoaded && modulesLoaded && window.initMap && typeof window.initMap === 'function') {
                console.log('Initializing map...');
                window.initMap();
            }
        }
        
        // 加载 Google Maps API
        function loadGoogleMaps() {
            const script = document.createElement('script');
            script.src = 'https://maps.googleapis.com/maps/api/js?key=AIzaSyB1FRS7vri1nRofZD5tFaXlVCwhcn2AvkY&libraries=places';
            script.async = true;
            script.defer = true;
            script.onload = function() {
                console.log('Google Maps API loaded');
                mapsApiLoaded = true;
                tryInitMap();
            };
            script.onerror = function() {
                console.error('Failed to load Google Maps API');
            };
            document.head.appendChild(script);
        }
        
        // 监听模块加载完成（通过检查 window.initMap 是否存在）
        function checkModulesLoaded() {
            if (window.initMap && typeof window.initMap === 'function') {
                console.log('Modules loaded');
                modulesLoaded = true;
                tryInitMap();
            } else {
                // 如果还没加载完，继续等待
                setTimeout(checkModulesLoaded, 50);
            }
        }
        
        // 开始检查模块加载状态
        checkModulesLoaded();
        
        // DOM 加载完成后加载 Google Maps
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', loadGoogleMaps);
        } else {
            loadGoogleMaps();
        }
    </script>

</html>

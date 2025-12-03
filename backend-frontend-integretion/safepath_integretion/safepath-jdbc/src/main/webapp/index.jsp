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
                <button id="filter-type-btn" class="filter-btn">Type</button>
                <button id="filter-time-btn" class="filter-btn">Time</button>
                <button id="clear-filters-btn" class="filter-btn">Clear</button>
            </div>
            
            <!-- Filter panels (hidden by default) -->
            <div id="filter-type-panel" class="filter-panel" style="display: none;">
                <h4>Filter by Crime Type</h4>
                <div id="crime-type-checkboxes" class="checkbox-group">
                    <!-- Will be populated dynamically -->
                </div>
            </div>
            
            <div id="filter-time-panel" class="filter-panel" style="display: none;">
                <h4>Filter by Time Range</h4>
                <div class="time-buttons">
                    <button class="time-btn" onclick="setTimeRangeHours(24)">Last 24h</button>
                    <button class="time-btn" onclick="setTimeRange(7)">Last 7 days</button>
                    <button class="time-btn" onclick="setTimeRange(30)">Last 30 days</button>
                    <button class="time-btn" onclick="setTimeRange(90)">Last 90 days</button>
                </div>
                <div class="custom-time">
                    <label>Custom Range:</label>
                    <input type="datetime-local" id="time-start" onchange="applyCustomTimeFilter()" />
                    <input type="datetime-local" id="time-end" onchange="applyCustomTimeFilter()" />
                </div>
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


    <!-- load map.js and crime.js first -->
    <script type="module" src="${pageContext.request.contextPath}/js/map.js"></script>
    <script type="module" src="${pageContext.request.contextPath}/js/crime.js"></script>

    <!-- Google Map load, manually initialize to ensure modules are loaded -->
    <script>
        // wait for modules and Google Maps API to be loaded before initializing the map
        let mapsApiLoaded = false;
        let modulesLoaded = false;
        
        function tryInitMap() {
            if (mapsApiLoaded && modulesLoaded && window.initMap && typeof window.initMap === 'function') {
                console.log('Initializing map...');
                window.initMap();
            }
        }
        
        // load Google Maps API
        function loadGoogleMaps() {
            const script = document.createElement('script');
            script.src = 'https://maps.googleapis.com/maps/api/js?key=AIzaSyB1FRS7vri1nRofZD5tFaXlVCwhcn2AvkY&libraries=places&language=en';
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
        
        // listen for modules loaded (by checking if window.initMap exists)
        function checkModulesLoaded() {
            if (window.initMap && typeof window.initMap === 'function') {
                console.log('Modules loaded');
                modulesLoaded = true;
                tryInitMap();
            } else {
                // if not loaded yet, continue waiting
                setTimeout(checkModulesLoaded, 50);
            }
        }
        
        // start checking module loading status
        checkModulesLoaded();
        
        // load Google Maps after DOM loaded
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', loadGoogleMaps);
        } else {
            loadGoogleMaps();
        }
        
        // Expose filter functions globally for inline onclick handlers
        window.applyTypeFilter = async function() {
            const module = await import('./js/crime.js');
            module.applyTypeFilter();
        };
        
        window.setTimeRange = async function(days) {
            const module = await import('./js/crime.js');
            module.setTimeRange(days);
        };
        window.setTimeRangeHours = async function(hours) {
            const module = await import('./js/crime.js');
            module.setTimeRangeHours(hours);
        };
        
        window.applyCustomTimeFilter = async function() {
            const module = await import('./js/crime.js');
            module.applyCustomTimeFilter();
        };
    </script>

</html>

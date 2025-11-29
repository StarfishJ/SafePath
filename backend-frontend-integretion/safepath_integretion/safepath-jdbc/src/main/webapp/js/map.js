import { loadCrimeByRange, loadCrimeByRangeWithFilters, renderCrimeList, addCrimeMarkers, initFilters, crimeMarkers } from "./crime.js";

let map;
let directionsService;

let routePolylines = [];   // polyline for each route
let markers = [];          // start and end markers
let selectedRouteIndex = null;

// ===================================================
// initialize the map
// ===================================================
function initMap() {
    const center = { lat: 47.6062, lng: -122.3321 };

    console.log("🗺️ Creating map instance...");
    map = new google.maps.Map(document.getElementById("map"), {
        zoom: 14,
        center,
    });
    
    console.log("🗺️ Map created:", map);
    console.log("🗺️ Map instanceof google.maps.Map?", map instanceof google.maps.Map);
    
    // mount the map to window, for other modules usage
    window.map = map;
    
    console.log("🗺️ window.map assigned:", window.map);
    console.log("🗺️ window.map instanceof google.maps.Map?", window.map instanceof google.maps.Map);

    directionsService = new google.maps.DirectionsService();

    initAutocomplete();

    // Initialize filter UI (async - loads crime types from backend)
    initFilters().then(() => {
        console.log("✅ Filters initialized");
    }).catch(err => {
        console.error("❌ Failed to initialize filters:", err);
    });

    // use the map range after the map is initially loaded to load crime data
    google.maps.event.addListenerOnce(map, "idle", () => {
        refreshCrimeByMapBounds();
    });

    // when the user drags or zooms the map → automatically load new range data
    map.addListener("idle", refreshCrimeByMapBounds);

    // listen for window resize
    window.addEventListener('resize', () => {
        if (map) google.maps.event.trigger(map, 'resize');
    });

    const mapElement = document.getElementById("map");
    if (mapElement && window.ResizeObserver) {
        const resizeObserver = new ResizeObserver(() => {
            if (map) google.maps.event.trigger(map, 'resize');
        });
        resizeObserver.observe(mapElement);
    }
}


// ===================================================
// automatically load crime data based on the map range
// ===================================================
async function refreshCrimeByMapBounds() {
    console.log("📍 refreshCrimeByMapBounds called");
    console.log("📍 map exists?", !!map);
    console.log("📍 window.map exists?", !!window.map);
    console.log("📍 map instanceof google.maps.Map?", map instanceof google.maps.Map);
    
    if (!map) {
        console.error("❌ map is not available in refreshCrimeByMapBounds");
        return;
    }

    const bounds = map.getBounds();
    if (!bounds) {
        console.warn("⚠️ map.getBounds() returned null/undefined");
        return;
    }

    const ne = bounds.getNorthEast();
    const sw = bounds.getSouthWest();

    const minLat = sw.lat();
    const maxLat = ne.lat();
    const minLon = sw.lng();
    const maxLon = ne.lng();

    console.log("📍 Map range:", { minLat, maxLat, minLon, maxLon });

    // Use filtered loading if filters are active, otherwise use default range
    let crimes;
    try {
        crimes = await loadCrimeByRangeWithFilters(minLat, maxLat, minLon, maxLon);
    } catch (e) {
        console.warn("⚠️ Filtered load failed, falling back to default:", e);
        crimes = await loadCrimeByRange(minLat, maxLat, minLon, maxLon, 50);
    }

    console.log("📍 Number of crimes loaded:", crimes.length);
    console.log("📍 Before calling addCrimeMarkers - window.map exists?", !!window.map);
    console.log("📍 Before calling addCrimeMarkers - window.map instanceof google.maps.Map?", window.map instanceof google.maps.Map);

    // render the list and markers
    renderCrimeList(crimes);
    addCrimeMarkers(crimes);
    
    if (crimes.length > 0) {
        console.log("✅ Successfully added", crimes.length, "markers to the map");
    } else {
        console.warn("⚠️ No crimes found in the current map range");
    }
}

// Expose refresh function globally for filter callbacks
window.refreshCrimeData = refreshCrimeByMapBounds;


// ===================================================
// other route planning code
// ===================================================
function initAutocomplete() {
    const inputFrom = document.getElementById("location-input");
    const inputTo = document.querySelector(".search-destination");

    const acFrom = new google.maps.places.Autocomplete(inputFrom);
    const acTo = new google.maps.places.Autocomplete(inputTo);

    acFrom.addListener("place_changed", () => {
        const place = acFrom.getPlace();
        if (!place || !place.geometry) return;

        map.setCenter(place.geometry.location);
        map.setZoom(15);

        addMarker(place.geometry.location);
    });

    acTo.addListener("place_changed", () => {
        calculateRoute();
    });
}


// ---------- Tools ----------
function addMarker(position) {
    const m = new google.maps.Marker({
        map,
        position
    });
    markers.push(m);
}

function clearMapObjects() {
    routePolylines.forEach(p => p.setMap(null));
    routePolylines = [];

    markers.forEach(m => m.setMap(null));
    markers = [];
}


// ===================================================
// Route calculation (integrate risk scoring)
// ===================================================
async function calculateRoute() {
    const origin = document.getElementById("location-input").value;
    const destination = document.querySelector(".search-destination").value;

    if (!origin || !destination) {
        alert("Please enter the origin and destination");
        return;
    }

    directionsService.route({
        origin,
        destination,
        travelMode: "DRIVING",
        provideRouteAlternatives: true,
        language: "en",  // Force English language for route names and instructions
    }, async (result, status) => {

        if (status !== "OK") {
            alert("Route planning failed: " + status);
            return;
        }

        clearMapObjects();
        
        // call the backend API to calculate the route risk
        let riskAnalysis = null;
        try {
            console.log("📊 Calculating route risk score...");
            console.log("📊 Google Directions result:", result);
            
            // convert the Google Directions Service result format to the format expected by the backend
            const requestData = {
                routes: result.routes.map(route => ({
                    summary: route.summary || "",
                    legs: route.legs.map(leg => ({
                        steps: leg.steps.map(step => ({
                            instructions: step.instructions || "",
                            distance: step.distance ? {
                                text: step.distance.text || "",
                                value: step.distance.value || 0
                            } : null,
                            duration: step.duration ? {
                                text: step.duration.text || "",
                                value: step.duration.value || 0
                            } : null,
                            start_location: step.start_location ? {
                                lat: step.start_location.lat(),
                                lng: step.start_location.lng()
                            } : null,
                            end_location: step.end_location ? {
                                lat: step.end_location.lat(),
                                lng: step.end_location.lng()
                            } : null,
                            polyline: step.polyline ? {
                                points: step.polyline.points || ""
                            } : null
                        }))
                    }))
                }))
            };
            
            console.log("📊 Sending data to backend:", requestData);
            
            const response = await fetch("http://localhost:8081/api/routes/risk", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(requestData)
            });
            
            console.log("📊 API response status:", response.status, response.statusText);
            
            if (response.ok) {
                riskAnalysis = await response.json();
                console.log("✅ Route risk analysis result:", riskAnalysis);
            } else {
                const errorText = await response.text();
                console.warn("⚠️ Route risk analysis API call failed:", response.status, errorText);
            }
        } catch (error) {
            console.error("❌ Error calling route risk analysis API:", error);
            console.error("❌ Error details:", error.message, error.stack);
        }
        
        drawRoutes(result, riskAnalysis);
        showRouteDetails(result, riskAnalysis);
    });
}


// ---------- draw multiple routes ----------
function drawRoutes(result, riskAnalysis) {
    const routes = result.routes.slice(0, 3);

    routes.forEach((route, index) => {
        const line = new google.maps.Polyline({
            map,
            path: route.overview_path,
            strokeColor: "#888",
            strokeOpacity: 0.7,
            strokeWeight: 4
        });

        routePolylines.push(line);
    });

    const firstLeg = result.routes[0].legs[0];
    addMarker(firstLeg.start_location);
    addMarker(firstLeg.end_location);
}


// ---------- Show route details (including risk score)----------
function showRouteDetails(result, riskAnalysis) {
    const container = document.getElementById("route-details");
    container.innerHTML = "";

    const routes = result.routes.slice(0, 3);

    // if there is risk analysis, sort the routes by risk score (the safest routes first)
    let sortedRoutes = [...routes];
    if (riskAnalysis && riskAnalysis.routes) {
        sortedRoutes = routes.map((route, index) => ({
            route,
            index,
            riskData: riskAnalysis.routes[index] || null
        })).sort((a, b) => {
            const scoreA = a.riskData ? a.riskData.totalRiskScore : 999;
            const scoreB = b.riskData ? b.riskData.totalRiskScore : 999;
            return scoreA - scoreB;
        });
    }

    sortedRoutes.forEach((item, displayIndex) => {
        const route = item.route || item;
        const originalIndex = item.index !== undefined ? item.index : displayIndex;
        const riskData = item.riskData || (riskAnalysis && riskAnalysis.routes[originalIndex]) || null;
        const leg = route.legs[0];

        const block = document.createElement("div");
        block.className = "route-block";

        const header = document.createElement("div");
        header.className = "route-header";
        
        // build the risk score display
        let riskBadge = "";
        if (riskData) {
            const riskScore = riskData.totalRiskScore;
            riskBadge = `<span style="margin-left: 10px;">
                Risk score: ${riskScore.toFixed(2)}
            </span>`;
        }
        
        header.innerHTML = `
            <span>Route ${displayIndex + 1}</span>
            <span>${leg.distance.text} • ${leg.duration.text}${riskBadge}</span>
            <span class="arrow">▼</span>
        `;

        const content = document.createElement("div");
        content.className = "route-content";

        let list = "<ol>";
        leg.steps.forEach((step, stepIndex) => {
            let stepRiskInfo = "";
            if (riskData && riskData.stepRisks && riskData.stepRisks[stepIndex]) {
                const stepRisk = riskData.stepRisks[stepIndex];
                const stepLabel = stepRisk.dominantRiskLabel || "UNKNOWN";
                stepRiskInfo = ` <span style="font-size: 0.9em;">
                    [${stepLabel}: ${stepRisk.averageRiskScore.toFixed(2)}]
                </span>`;
            }
            list += `<li>${step.instructions} (${step.distance.text})${stepRiskInfo}</li>`;
        });
        list += "</ol>";
        content.innerHTML = list;

        header.addEventListener("click", (e) => {
            e.stopPropagation();
            content.classList.toggle("open");
            header.querySelector(".arrow").classList.toggle("open");
        });

        block.addEventListener("mouseenter", () => highlightRoute(originalIndex));
        block.addEventListener("mouseleave", () => {
            if (selectedRouteIndex !== originalIndex) resetRoute(originalIndex);
        });

        block.addEventListener("click", () => {
            selectedRouteIndex = originalIndex;
            highlightExclusive(originalIndex);
        });

        block.appendChild(header);
        block.appendChild(content);
        container.appendChild(block);
    });
}


// ---------- Highlight ----------
function highlightRoute(i) {
    routePolylines[i].setOptions({
        strokeColor: "#1976ff",
        strokeWeight: 6,
        strokeOpacity: 1
    });
}

function resetRoute(i) {
    routePolylines[i].setOptions({
        strokeColor: "#888",
        strokeOpacity: 0.6,
        strokeWeight: 4
    });
}

function highlightExclusive(i) {
    routePolylines.forEach((pl, idx) => {
        if (idx === i) {
            pl.setOptions({
                strokeColor: "#1976ff",
                strokeWeight: 6,
                strokeOpacity: 1
            });
        } else {
            pl.setOptions({
                strokeColor: "#bbb",
                strokeOpacity: 0.2,
                strokeWeight: 3
            });
        }
    });
}

// mount initMap to window, for Google Maps API callback usage
// it must be mounted immediately when the module is loaded, because the Google Maps API may load asynchronously
window.initMap = initMap;

console.log("map.js loaded (with crime integration).");

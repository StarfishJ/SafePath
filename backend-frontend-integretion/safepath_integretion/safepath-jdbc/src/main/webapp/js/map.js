import { loadCrimeByRange, renderCrimeList, addCrimeMarkers } from "./crime.js";

let map;
let directionsService;

let routePolylines = [];   // 各路线 polyline
let markers = [];          // 起点终点 marker
let selectedRouteIndex = null;

// ===================================================
// 初始化地图
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
    
    // 将 map 挂载到 window，供其他模块使用
    window.map = map;
    
    console.log("🗺️ window.map assigned:", window.map);
    console.log("🗺️ window.map instanceof google.maps.Map?", window.map instanceof google.maps.Map);

    directionsService = new google.maps.DirectionsService();

    initAutocomplete();

    // 使用地图初次加载后的范围加载犯罪数据
    google.maps.event.addListenerOnce(map, "idle", () => {
        refreshCrimeByMapBounds();
    });

    // 当用户拖动或缩放地图后 → 自动加载新范围的数据
    map.addListener("idle", refreshCrimeByMapBounds);

    // 监听窗口变化
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
// 自动根据地图范围加载犯罪数据
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

    console.log("📍 地图范围：", { minLat, maxLat, minLon, maxLon });

    // 调用 crime.js 的 API，默认 50 天
    const crimes = await loadCrimeByRange(minLat, maxLat, minLon, maxLon, 50);

    console.log("📍 加载到的犯罪数据数量：", crimes.length);
    console.log("📍 Before calling addCrimeMarkers - window.map exists?", !!window.map);
    console.log("📍 Before calling addCrimeMarkers - window.map instanceof google.maps.Map?", window.map instanceof google.maps.Map);

    // 渲染列表、渲染 markers
    renderCrimeList(crimes);
    addCrimeMarkers(crimes);
    
    if (crimes.length > 0) {
        console.log("✅ 成功在地图上添加", crimes.length, "个标记点");
    } else {
        console.warn("⚠️ 当前地图范围内没有犯罪数据");
    }
}


// ===================================================
// 你的其余路线规划代码（保持原样）
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


// ---------- 工具 ----------
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
// 路线计算（完全保留）
// ===================================================
function calculateRoute() {
    const origin = document.getElementById("location-input").value;
    const destination = document.querySelector(".search-destination").value;

    if (!origin || !destination) {
        alert("请输入起点和终点");
        return;
    }

    directionsService.route({
        origin,
        destination,
        travelMode: "DRIVING",
        provideRouteAlternatives: true,
    }, (result, status) => {

        if (status !== "OK") {
            alert("路线规划失败：" + status);
            return;
        }

        clearMapObjects();
        drawRoutes(result);
        showRouteDetails(result);
    });
}


// ---------- 绘制多路线 ----------
function drawRoutes(result) {
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


// ---------- 展示路线详情 ----------
function showRouteDetails(result) {
    const container = document.getElementById("route-details");
    container.innerHTML = "";

    const routes = result.routes.slice(0, 3);

    routes.forEach((route, index) => {
        const leg = route.legs[0];

        const block = document.createElement("div");
        block.className = "route-block";

        const header = document.createElement("div");
        header.className = "route-header";
        header.innerHTML = `
            <span>路线 ${index + 1}</span>
            <span>${leg.distance.text} • ${leg.duration.text}</span>
            <span class="arrow">▼</span>
        `;

        const content = document.createElement("div");
        content.className = "route-content";

        let list = "<ol>";
        leg.steps.forEach(step => {
            list += `<li>${step.instructions} (${step.distance.text})</li>`;
        });
        list += "</ol>";
        content.innerHTML = list;

        header.addEventListener("click", (e) => {
            e.stopPropagation();
            content.classList.toggle("open");
            header.querySelector(".arrow").classList.toggle("open");
        });

        block.addEventListener("mouseenter", () => highlightRoute(index));
        block.addEventListener("mouseleave", () => {
            if (selectedRouteIndex !== index) resetRoute(index);
        });

        block.addEventListener("click", () => {
            selectedRouteIndex = index;
            highlightExclusive(index);
        });

        block.appendChild(header);
        block.appendChild(content);
        container.appendChild(block);
    });
}


// ---------- 高亮 ----------
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

// 将 initMap 挂载到 window，供 Google Maps API callback 使用
// 必须在模块加载时立即挂载，因为 Google Maps API 可能异步加载
window.initMap = initMap;

console.log("map.js loaded (with crime integration).");

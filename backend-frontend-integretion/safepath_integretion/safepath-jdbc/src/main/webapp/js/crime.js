// =============================
// crime.js — 负责加载犯罪数据、渲染列表、打点
// =============================

// 后端 API（保持一致）
const BASE_URL = "/crime-report";

export async function loadCrimeByRange(minLat, maxLat, minLon, maxLon, days = 30) {
    const endTime = new Date();
    const startTime = new Date(endTime.getTime() - days * 24 * 60 * 60 * 1000);

    const start = startTime.toISOString().slice(0, 16);
    const end = endTime.toISOString().slice(0, 16);

    const params = new URLSearchParams({
        action: "range",
        min_lat: minLat,
        max_lat: maxLat,
        min_lng: minLon,
        max_lng: maxLon,
        start_date: start,
        end_date: end
    });

    const url = `${BASE_URL}?${params.toString()}`;

    console.log("📡 Fetch crimes:", url);
    console.log("📡 Parameters:", { minLat, maxLat, minLon, maxLon, start, end });

    try {
        const res = await fetch(url);
        console.log("📡 Response status:", res.status, res.statusText);
        if (!res.ok) {
            const errorText = await res.text();
            console.error("❌ Crime fetch error:", res.status, res.statusText);
            console.error("❌ Error response:", errorText);
            return [];
        }
        const data = await res.json();
        console.log("✅ Received", data.length, "crimes");
        if (data.length === 0) {
            console.warn("⚠️ No crimes found in the specified range");
        }
        return data;
    } catch (e) {
        console.error("❌ Network error:", e);
        console.error("❌ Error details:", e.message);
        return [];
    }
}

// =============================
// 渲染侧边栏
// =============================
export function renderCrimeList(crimes) {
    const container = document.querySelector(".alert-list");
    if (!container) return;

    container.innerHTML = "";

    crimes.forEach(c => {
        const div = document.createElement("div");
        div.className = "alert-item";
        div.innerHTML = `
            <div><b>${c.crimeType || "Crime"}</b></div>
            <div>${c.description || "No description"}</div>
            <div>(${c.latitude?.toFixed(4)}, ${c.longitude?.toFixed(4)})</div>
        `;
        container.appendChild(div);
    });
}

// =============================
// 渲染 Marker
// =============================
export let crimeMarkers = [];

export function addCrimeMarkers(crimes) {
    console.log("🔍 addCrimeMarkers called with", crimes.length, "crimes");
    console.log("🔍 window.map exists?", !!window.map);
    console.log("🔍 window.map type?", typeof window.map);
    console.log("🔍 window.map instanceof google.maps.Map?", window.map instanceof google.maps.Map);
    
    if (!window.map) {
        console.error("❌ window.map is not available");
        return;
    }
    
    if (!(window.map instanceof google.maps.Map)) {
        console.error("❌ window.map is not a valid Google Maps instance");
        return;
    }

    // 清除旧 marker
    crimeMarkers.forEach(m => m.setMap(null));
    crimeMarkers = [];

    crimes.forEach(c => {
        if (c.latitude == null || c.longitude == null) return;
        try {
            const marker = new google.maps.Marker({
                position: { lat: c.latitude, lng: c.longitude },
                map: window.map,
                icon: {
                    path: google.maps.SymbolPath.CIRCLE,
                    scale: 6,
                    fillColor: "#B8860B",  // 深黄色 (dark goldenrod)
                    fillOpacity: 0.8,
                    strokeColor: "#8B6914",  // 更深的边框
                    strokeWeight: 1.5
                }
            });
            crimeMarkers.push(marker);
        } catch (error) {
            console.error("❌ Error creating marker:", error);
        }
    });
    
    console.log("✅ Successfully added", crimeMarkers.length, "markers");
}

console.log("crime.js loaded.");

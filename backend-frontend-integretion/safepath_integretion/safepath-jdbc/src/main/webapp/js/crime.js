// =============================
// crime.js — load crime data, render list, add markers
// =============================

// backend API (keep consistent)
const BASE_URL = "/crime-report";

// global filter state
let currentFilters = {
    crimeTypes: [],
    timeRange: null, // { start: Date, end: Date } or { days: number }
    customTimeStart: null,
    customTimeEnd: null
};

// Crime types - will be loaded dynamically from backend
let COMMON_CRIME_TYPES = [
    "Assault",
    "Robbery",
    "Burglary",
    "Larceny/Theft",
    "Motor Vehicle Theft",
    "Destruction/Damage/Vandalism",
    "Homicide",
    "Arson",
    "Drug/Narcotic",
    "Weapon Law Violations"
];

// Load available crime types from backend
export async function loadCrimeTypes() {
    try {
        const url = `${BASE_URL}?action=crimeTypes`;
        const res = await fetch(url);
        if (res.ok) {
            const types = await res.json();
            if (Array.isArray(types) && types.length > 0) {
                COMMON_CRIME_TYPES = types;
                console.log("✅ Loaded crime types from backend:", COMMON_CRIME_TYPES);
                return types;
            }
        }
        console.warn("⚠️ Failed to load crime types from backend, using defaults");
        return COMMON_CRIME_TYPES;
    } catch (e) {
        console.error("❌ Error loading crime types:", e);
        return COMMON_CRIME_TYPES;
    }
}

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
        let data = await res.json();
        console.log("✅ Received", data.length, "crimes");
        
        // Apply client-side filters if any
        data = applyClientSideFilters(data);
        
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

// Apply client-side filters (for type filtering if not supported by backend)
function applyClientSideFilters(crimes) {
    // If no crime types are selected, return all crimes (no filtering)
    if (!currentFilters.crimeTypes || currentFilters.crimeTypes.length === 0) {
        return crimes;
    }
    
    // Filter crimes by selected types
    return crimes.filter(crime => {
        // Get crime type from either crimeType or offenseType field
        const crimeTypeStr = (crime.crimeType || crime.offenseType || "").toLowerCase();
        
        // If crime has no type information, include it (don't filter it out)
        if (!crimeTypeStr || crimeTypeStr.trim() === "") {
            return true;
        }
        
        // Split by comma to handle multiple types (e.g., "Assault, Robbery")
        const crimeTypes = crimeTypeStr.split(',').map(t => t.trim()).filter(t => t.length > 0);
        
        // Check if any of the selected filter types match any of the crime's types
        return currentFilters.crimeTypes.some(filterType => {
            const filterLower = filterType.toLowerCase();
            return crimeTypes.some(ct => ct.includes(filterLower) || filterLower.includes(ct));
        });
    });
}

// Load crimes with filters
export async function loadCrimeByRangeWithFilters(minLat, maxLat, minLon, maxLon) {
    // Always use the filter API to ensure consistent results
    // The filter API handles both cases: with and without type filters
    
    const bounds = window.map?.getBounds();
    let lat, lon, radius;
    
    if (!bounds) {
        // Fallback: calculate center from provided bounds
        lat = (minLat + maxLat) / 2;
        lon = (minLon + maxLon) / 2;
        // Calculate approximate radius from bounds (in meters)
        const latDiff = maxLat - minLat;
        const lonDiff = maxLon - minLon;
        const avgLat = (minLat + maxLat) / 2;
        const latMeters = latDiff * 111000; // 1 degree lat ≈ 111km
        const lonMeters = lonDiff * 111000 * Math.cos(avgLat * Math.PI / 180);
        radius = Math.max(latMeters, lonMeters) * 1.5; // Add 50% buffer
    } else {
        const center = bounds.getCenter();
        lat = center.lat();
        lon = center.lng();
        // Calculate radius from bounds to ensure we get all crimes in the visible area
        const ne = bounds.getNorthEast();
        const sw = bounds.getSouthWest();
        const latDiff = ne.lat() - sw.lat();
        const lonDiff = ne.lng() - sw.lng();
        const avgLat = (ne.lat() + sw.lat()) / 2;
        const latMeters = latDiff * 111000;
        const lonMeters = lonDiff * 111000 * Math.cos(avgLat * Math.PI / 180);
        radius = Math.max(latMeters, lonMeters) * 1.5; // Add 50% buffer to ensure we get all crimes
    }
    
    const params = new URLSearchParams({
        action: "filter",
        lat: lat,
        lon: lon,
        radius: Math.round(radius), // Round to integer for backend compatibility
        limit: 500
    });
    
    // Add crime types filter (only if types are selected)
    // If no types are selected, don't add this parameter, so backend returns all types
    if (currentFilters.crimeTypes && currentFilters.crimeTypes.length > 0) {
        params.append("crimeTypes", currentFilters.crimeTypes.join(","));
    }
    
    // Add time range filter
    let startTime = null;
    let endTime = null;
    
    if (currentFilters.customTimeStart && currentFilters.customTimeEnd) {
        // Custom time range
        startTime = new Date(currentFilters.customTimeStart);
        endTime = new Date(currentFilters.customTimeEnd);
    } else if (currentFilters.timeRange?.days) {
        // Preset time range (24h, 7d, 30d, 90d)
        // days can be fractional (e.g., 1.0 for 24 hours)
        endTime = new Date();
        startTime = new Date(endTime.getTime() - currentFilters.timeRange.days * 24 * 60 * 60 * 1000);
    } else {
        // Default: last 30 days if no time filter is set
        endTime = new Date();
        startTime = new Date(endTime.getTime() - 30 * 24 * 60 * 60 * 1000);
    }
    
    // Always add time parameters to ensure consistent filtering
    // Format: "yyyy-MM-ddTHH:mm" (datetime-local format)
    const formatDateTime = (date) => {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        return `${year}-${month}-${day}T${hours}:${minutes}`;
    };
    
    params.append("timeStart", formatDateTime(startTime));
    params.append("timeEnd", formatDateTime(endTime));
    
    console.log("⏰ Time filter:", formatDateTime(startTime), "to", formatDateTime(endTime));
    
    const url = `${BASE_URL}?${params.toString()}`;
    console.log("📡 Fetch crimes with filters:", url);
    
    try {
        const res = await fetch(url);
        if (!res.ok) {
            console.error("❌ Filter fetch error:", res.status);
            return [];
        }
        const data = await res.json();
        const crimes = data.crimes || data;
        console.log("✅ Received", crimes.length, "filtered crimes");
        
        // Apply client-side filters if needed (for type filtering when not handled by backend)
        // Note: If no crime types are selected, this will return all crimes
        const filteredCrimes = applyClientSideFilters(crimes);
        console.log("✅ After client-side filtering:", filteredCrimes.length, "crimes");
        return filteredCrimes;
    } catch (e) {
        console.error("❌ Network error:", e);
        return [];
    }
}

// Initialize filter UI
export async function initFilters() {
    // Load crime types from backend first
    await loadCrimeTypes();
    
    // Populate crime type checkboxes
    const container = document.getElementById("crime-type-checkboxes");
    if (container) {
        container.innerHTML = "";
        if (COMMON_CRIME_TYPES.length === 0) {
            container.innerHTML = "<p style='padding: 10px; color: #666;'>No crime types available</p>";
        } else {
            COMMON_CRIME_TYPES.forEach(type => {
                const label = document.createElement("label");
                label.className = "checkbox-label";
                const checkbox = document.createElement("input");
                checkbox.type = "checkbox";
                checkbox.value = type;
                checkbox.className = "crime-type-checkbox";
                // Auto-apply filter when checkbox changes
                checkbox.addEventListener("change", () => {
                    applyTypeFilter();
                });
                label.appendChild(checkbox);
                const span = document.createElement("span");
                span.textContent = type;
                label.appendChild(span);
                container.appendChild(label);
            });
        }
    }
    
    // Setup filter button handlers
    const typeBtn = document.getElementById("filter-type-btn");
    const timeBtn = document.getElementById("filter-time-btn");
    const clearBtn = document.getElementById("clear-filters-btn");
    
    if (typeBtn) {
        typeBtn.addEventListener("click", () => {
            const panel = document.getElementById("filter-type-panel");
            const timePanel = document.getElementById("filter-time-panel");
            if (panel) {
                panel.style.display = panel.style.display === "none" ? "block" : "none";
                if (timePanel) timePanel.style.display = "none";
            }
        });
    }
    
    if (timeBtn) {
        timeBtn.addEventListener("click", () => {
            const panel = document.getElementById("filter-time-panel");
            const typePanel = document.getElementById("filter-type-panel");
            if (panel) {
                panel.style.display = panel.style.display === "none" ? "block" : "none";
                if (typePanel) typePanel.style.display = "none";
            }
        });
    }
    
    if (clearBtn) {
        clearBtn.addEventListener("click", () => {
            clearFilters();
        });
    }
}

// Apply type filter (auto-called when checkbox changes)
export function applyTypeFilter() {
    const checkboxes = document.querySelectorAll(".crime-type-checkbox:checked");
    currentFilters.crimeTypes = Array.from(checkboxes).map(cb => cb.value);
    console.log("🔍 Applied type filter:", currentFilters.crimeTypes);
    
    // Refresh crime data automatically
    if (window.refreshCrimeData) {
        window.refreshCrimeData();
    }
}

// Set time range (in days)
export function setTimeRange(days) {
    currentFilters.timeRange = { days };
    currentFilters.customTimeStart = null;
    currentFilters.customTimeEnd = null;
    console.log("🔍 Applied time range:", days, "days");
    
    // Update active button state
    document.querySelectorAll(".time-btn").forEach(btn => {
        btn.classList.remove("active");
        if (btn.textContent.includes(days + " days")) {
            btn.classList.add("active");
        }
    });
    
    // Refresh crime data
    if (window.refreshCrimeData) {
        window.refreshCrimeData();
    }
}

// Set time range (in hours) - for 24h button
export function setTimeRangeHours(hours) {
    // Convert hours to fractional days for consistency
    const days = hours / 24.0;
    currentFilters.timeRange = { days };
    currentFilters.customTimeStart = null;
    currentFilters.customTimeEnd = null;
    console.log("🔍 Applied time range:", hours, "hours (", days, "days)");
    
    // Update active button state
    document.querySelectorAll(".time-btn").forEach(btn => {
        btn.classList.remove("active");
        if (btn.textContent.includes("24h")) {
            btn.classList.add("active");
        }
    });
    
    // Refresh crime data
    if (window.refreshCrimeData) {
        window.refreshCrimeData();
    }
}

// Apply custom time filter (auto-called when time inputs change)
export function applyCustomTimeFilter() {
    const startInput = document.getElementById("time-start");
    const endInput = document.getElementById("time-end");
    
    if (startInput && endInput && startInput.value && endInput.value) {
        currentFilters.customTimeStart = startInput.value;
        currentFilters.customTimeEnd = endInput.value;
        currentFilters.timeRange = null;
        console.log("🔍 Applied custom time range:", currentFilters.customTimeStart, "to", currentFilters.customTimeEnd);
        
        // Clear active time button state
        document.querySelectorAll(".time-btn").forEach(btn => btn.classList.remove("active"));
        
        // Refresh crime data automatically
        if (window.refreshCrimeData) {
            window.refreshCrimeData();
        }
    }
}

// Clear all filters
export function clearFilters() {
    currentFilters = {
        crimeTypes: [],
        timeRange: null,
        customTimeStart: null,
        customTimeEnd: null
    };
    
    // Uncheck all checkboxes
    document.querySelectorAll(".crime-type-checkbox").forEach(cb => cb.checked = false);
    
    // Clear time inputs
    const startInput = document.getElementById("time-start");
    const endInput = document.getElementById("time-end");
    if (startInput) startInput.value = "";
    if (endInput) endInput.value = "";
    
    console.log("🔍 Cleared all filters");
    
    // Refresh crime data
    if (window.refreshCrimeData) {
        window.refreshCrimeData();
    }
}

// =============================
// render sidebar
// =============================
export function renderCrimeList(crimes) {
    const container = document.querySelector(".alert-list");
    if (!container) return;

    container.innerHTML = "";

    crimes.forEach(c => {
        const div = document.createElement("div");
        div.className = "alert-item";
        div.style.cursor = "pointer";
        div.innerHTML = `
            <div><b>${c.crimeType || c.offenseType || "Crime"}</b></div>
            <div>${c.description || c.neighborhood || "No description"}</div>
            <div style="font-size: 0.9em; color: #666;">
                ${c.reportDatetime ? new Date(c.reportDatetime).toLocaleDateString() : ""}
                ${c.precinct ? ` • ${c.precinct}` : ""}
            </div>
        `;
        
        // Add click event to scroll to marker on map and show info
        div.addEventListener("click", () => {
            if (c.latitude && c.longitude && window.map) {
                window.map.setCenter({ lat: c.latitude, lng: c.longitude });
                window.map.setZoom(16);
                
                // Find and trigger click on corresponding marker
                // Use a small delay to ensure map has moved
                setTimeout(() => {
                    const marker = crimeMarkers.find(m => {
                        if (!m.crimeData) return false;
                        // Match by report number or by position (with small tolerance)
                        return m.crimeData.reportNumber === c.reportNumber ||
                               (Math.abs(m.crimeData.latitude - c.latitude) < 0.0001 &&
                                Math.abs(m.crimeData.longitude - c.longitude) < 0.0001);
                    });
                    if (marker) {
                        google.maps.event.trigger(marker, "click");
                    } else {
                        // If marker not found, create a temporary info window
                        showCrimeInfoWindow(c, { lat: c.latitude, lng: c.longitude });
                    }
                }, 300);
            }
        });
        
        container.appendChild(div);
    });
}

// =============================
// render markers
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

    // clear old markers
    crimeMarkers.forEach(m => {
        if (m.infoWindow) {
            m.infoWindow.close();
        }
        m.setMap(null);
    });
    crimeMarkers = [];

    // Create InfoWindow instance (reuse for all markers)
    let infoWindow = new google.maps.InfoWindow();

    crimes.forEach(c => {
        if (c.latitude == null || c.longitude == null) return;
        try {
            const marker = new google.maps.Marker({
                position: { lat: c.latitude, lng: c.longitude },
                map: window.map,
                icon: {
                    path: google.maps.SymbolPath.CIRCLE,
                    scale: 6,
                    fillColor: "#B8860B",  // dark goldenrod
                    fillOpacity: 0.8,
                    strokeColor: "#8B6914",  // darker border
                    strokeWeight: 1.5
                },
                title: c.crimeType || c.offenseType || "Crime"  // Tooltip on hover
            });
            
            // Store crime data in marker for click event
            marker.crimeData = c;
            
            // Add click event listener to show crime details
            marker.addListener("click", () => {
                showCrimeInfoWindow(c, marker);
            });
            
            crimeMarkers.push(marker);
        } catch (error) {
            console.error("❌ Error creating marker:", error);
        }
    });
    
    console.log("✅ Successfully added", crimeMarkers.length, "markers");
}

// Global InfoWindow instance (reuse for all markers)
let globalInfoWindow = null;

// Function to show crime information in InfoWindow
function showCrimeInfoWindow(crime, markerOrPosition) {
    if (!window.map) return;
    
    // Create InfoWindow if it doesn't exist
    if (!globalInfoWindow) {
        globalInfoWindow = new google.maps.InfoWindow();
    }
    
    // Format crime information
    const crimeType = crime.crimeType || crime.offenseType || "Unknown";
    const description = crime.description || crime.neighborhood || "No description";
    const reportNumber = crime.reportNumber || "N/A";
    const reportDate = crime.reportDatetime ? new Date(crime.reportDatetime).toLocaleString() : "N/A";
    const precinct = crime.precinct || "N/A";
    const sector = crime.sector || "N/A";
    const beat = crime.beat || "N/A";
    const neighborhood = crime.neighborhood || "N/A";
    
    const content = `
        <div style="padding: 10px; min-width: 250px; font-family: Arial, sans-serif;">
            <h3 style="margin: 0 0 10px 0; color: #333; font-size: 16px; border-bottom: 2px solid #B8860B; padding-bottom: 5px;">${escapeHtml(crimeType)}</h3>
            <div style="margin-bottom: 8px; line-height: 1.6;">
                <div style="margin-bottom: 5px;"><strong>Report Number:</strong> ${escapeHtml(reportNumber)}</div>
                <div style="margin-bottom: 5px;"><strong>Date:</strong> ${escapeHtml(reportDate)}</div>
                <div style="margin-bottom: 5px;"><strong>Location:</strong> ${escapeHtml(description)}</div>
                <div style="margin-bottom: 5px;"><strong>Neighborhood:</strong> ${escapeHtml(neighborhood)}</div>
                <div style="margin-bottom: 5px;"><strong>Precinct:</strong> ${escapeHtml(precinct)}</div>
                <div style="margin-bottom: 5px;"><strong>Sector:</strong> ${escapeHtml(sector)}</div>
                <div style="margin-bottom: 5px;"><strong>Beat:</strong> ${escapeHtml(beat)}</div>
                <div style="margin-top: 8px; padding-top: 8px; border-top: 1px solid #ddd; font-size: 0.9em; color: #666;">
                    <strong>Coordinates:</strong> ${crime.latitude?.toFixed(6)}, ${crime.longitude?.toFixed(6)}
                </div>
            </div>
        </div>
    `;
    
    // Close any existing info window and open new one
    globalInfoWindow.close();
    globalInfoWindow.setContent(content);
    
    // Determine position: if markerOrPosition is a marker, use it; otherwise use the position object
    if (markerOrPosition instanceof google.maps.Marker) {
        globalInfoWindow.open(window.map, markerOrPosition);
    } else {
        globalInfoWindow.setPosition(markerOrPosition);
        globalInfoWindow.open(window.map);
    }
}

// Helper function to escape HTML for security
function escapeHtml(text) {
    if (text == null) return "";
    const div = document.createElement("div");
    div.textContent = text;
    return div.innerHTML;
}

console.log("crime.js loaded.");

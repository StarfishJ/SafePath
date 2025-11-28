# Route Risk Scoring API

## Overview

The Route Risk API accepts one or more candidate routes (as returned by Google Directions API) and computes a safety risk score for each route and step by mapping polyline coordinates to our ML-generated street segment risk data.

**Use case**: Frontend fetches multiple paths from Google Directions, sends them to this endpoint, and receives back which route is safest.

---

## Endpoint

**POST** `/api/routes/risk`

### Request Body

Send the same JSON structure you get from Google Directions API. Key fields:

```json
{
  "routes": [
    {
      "summary": "via 1st Ave",
      "legs": [
        {
          "steps": [
            {
              "instructions": "Head north on Pine St",
              "distance": {
                "text": "0.5 km",
                "value": 500
              },
              "duration": {
                "text": "2 mins",
                "value": 120
              },
              "start_location": {
                "lat": 47.6062,
                "lng": -122.3321
              },
              "end_location": {
                "lat": 47.6112,
                "lng": -122.3321
              },
              "polyline": {
                "points": "encoded_polyline_string"
              }
            }
          ]
        }
      ]
    }
  ]
}
```

### Response Body

Returns risk analysis per route and per step:

```json
{
  "routes": [
    {
      "routeName": "via 1st Ave",
      "totalRiskScore": 0.42,
      "totalSteps": 5,
      "stepRisks": [
        {
          "averageRiskScore": 0.65,
          "dominantRiskLabel": "HIGH"
        },
        {
          "averageRiskScore": 0.28,
          "dominantRiskLabel": "MEDIUM"
        }
      ]
    }
  ]
}
```

**Field definitions**:

- `routeName`: Google's route summary or `"Route N"` if missing.
- `totalRiskScore`: average of all step risk scores (0–1 range).
- `totalSteps`: number of steps analyzed in this route.
- `stepRisks[].averageRiskScore`: mean risk score of all polyline points in that step.
- `stepRisks[].dominantRiskLabel`: the most common risk tier (`LOW`, `MEDIUM`, `HIGH`) seen in this step.

---

## How It Works

1. **Decode polyline**: Each step's `polyline.points` is decoded into a list of lat/lng coordinates (Google's encoded format).
2. **Nearest-segment lookup**: For each coordinate, run a database query to find the closest `street_segment` and fetch its `risk_score` and `risk_label` from the ML table.
3. **Aggregate per step**: average all point scores; count label frequencies to pick the dominant tier.
4. **Aggregate per route**: average all step scores to get the overall route risk.

---

## Integration Example

### Frontend workflow

```javascript
// 1. Get directions from Google
const directionsResponse = await fetch(
  `https://maps.googleapis.com/maps/api/directions/json?origin=...&destination=...&alternatives=true`
).then((res) => res.json());

// 2. Send to SafePath backend
const riskAnalysis = await fetch("/api/routes/risk", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify(directionsResponse),
}).then((res) => res.json());

// 3. Find safest route
const safestRoute = riskAnalysis.routes.sort(
  (a, b) => a.totalRiskScore - b.totalRiskScore
)[0];
console.log(
  `Safest: ${
    safestRoute.routeName
  } (risk = ${safestRoute.totalRiskScore.toFixed(2)})`
);
```

---

## Notes

- **Performance**: Each step's polyline may decode into dozens of points; for very long routes, consider sampling every Nth point to reduce DB queries.
- **Caching**: Frequently used steps (downtown corridors) can be cached to avoid repeated lookups.
- **Fallback**: If no nearby segment is found for a point (e.g., user is in a park), that point contributes zero risk and is skipped.
- **Time-of-day**: Current implementation uses static 90-day aggregates; future enhancements could weight scores by time-of-day or recent incident spikes.

---

## Testing

Run the integration test to verify the API:

```bash
mvn test -Dtest=RouteRiskIntegrationTest
```

The test seeds H2 with sample segments and sends a mock Google Directions payload, confirming the response structure and score ranges.

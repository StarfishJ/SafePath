# SafePath 🚶‍♂️  ongoing project

## Project Overview
SafePath is a data-driven navigation platform designed for urban pedestrians and city visitors. We integrate crime statistics, lighting conditions, and community-reported incidents to provide real-time safety alerts and safer walking route planning.

## Tech Stacks
Front End: Next.js(javascript)   - UI/SSR/SPA

Backend: Spring Boot(java)       - REST APIs, Spring Security (JWT/OAuth2), DB access

Database: PostgreSQL + PostGIS   - data storage & spatial queries

ML: Pandas, PyTorch(python)      - build and serve ML models (ETL, preprocessing, training, inference)

### Environment Configuration
- Duplicate `env.example` → `.env` and fill in your local MySQL credentials.
- The Python ETL and clustering jobs automatically read `.env` via `python-dotenv`.
- `.env` is ignored by git, so teammates can keep their own passwords private.

### Team Members
- Minglu Sun
- Hanhan Guo
- Yuchen Huang
- Jiaqi Guo
- Chichi Zhang

### Background
Traditional map applications have the following limitations:
- Lack of crime and lighting data
- Unable to reflect community safety concerns
- No automatic planning for safer walking routes

## Features

### Planned Features for This Semester
1. **Interactive Crime Map**
   - Display clustered pins on a map
   - Users can click to view detailed incident cards (type, time, location, city)

2. **Search & Filter Panel**
   - Filter crimes by type (multi-select)
   - Filter by city
   - Filter by time range (24h/7d/30d/custom)

3. **ETL Data Pipeline**
   - Clean, normalize, and deduplicate CSV crime data
   - Unified data schema
   - Load into spatially indexed database

4. **Safety Recommendations (Advanced)**
   - ML models trained on historical data
   - Suggest safer routes
   - Identify areas to avoid

### Safety Recommendation ML Pipeline
- `data-process-insert/segment_risk_clustering.py` clusters street segments each night (defaults: 90-day lookback, 3 clusters).  
  Run locally with `python data-process-insert/segment_risk_clustering.py` once MySQL is populated.
- Results persist to `street_segment_risk` (risk label, normalized score, feature summary, model version). Schema defined in `database-manipulation/create_table.sql`.
- Spring Boot exposes the data via:
  - `GET /api/safety/segments/{unitid}` – single segment risk snapshot.
  - `GET /api/safety/segments?west=...&south=...&east=...&north=...` – viewport query for maps and routing.
- Sample data for local H2 profile is seeded in `src/main/resources/data.sql`; integration tests live in `src/test/java/com/safepath/integration/SafetyRecommendationIntegrationTest.java`.

### Future Development Plans
SafePath will evolve into a comprehensive urban safety platform:

1. **Enhanced ML-Driven Routing**
   - Real-time crime risk prediction
   - Dynamic risk scores for road segments based on time, lighting, and recent local incidents
   - Real-time route adjustments

2. **Wearables and Mobile Device Integration**
   - Live safety alerts
   - Quick location sharing with emergency contacts

3. **Multi-City Expansion**
   - Integration of open crime and safety datasets
   - Broader geographic coverage and impact

## Data Sources and Processing

### 1. Historical Crime Data
**Source:** [Seattle Police Department Crime Data](https://data.seattle.gov/Public-Safety/SPD-Crime-Data-2008-Present/tazs-3rd5/about_data)

**Processing Method:**
- Daily API calls using Python (requests/pandas) to fetch new records
- Storage in PostgreSQL + PostGIS database
- Clean timestamps and coordinates
- Build spatial indexes for fast querying and map rendering

### 2. Real-Time Emergency Data
**Source:** [Seattle Real-Time Fire 911 Calls](https://data.seattle.gov/Public-Safety/Seattle-Real-Time-Fire-911-Calls/kzjm-xkqj/about_data)

**Processing Method:**
- Poll JSON API every 5 minutes or subscribe to real-time feed
- Parse responses and insert new records into "live incidents" table (Redis or PostgreSQL)

### 3. Street Network Data
**Source:** [Seattle Streets Dataset](https://data-seattlecitygis.opendata.arcgis.com/datasets/SeattleCityGIS::seattle-streets/about)

**Processing Method:**
- Download shapefile or GeoJSON
- Import into PostGIS using ogr2ogr or shp2pgsql

- Build routing topology (nodes, edges) to support shortest-path algorithms



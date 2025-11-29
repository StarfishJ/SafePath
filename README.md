# SafePath 🚶‍♂️  ongoing project

## Project Overview
SafePath is a data-driven navigation platform designed for urban pedestrians and city visitors. We integrate crime statistics, lighting conditions, and community-reported incidents to provide real-time safety alerts and safer walking route planning.

## Tech Stacks
- **Front End**: JSP (JavaServer Pages) + JavaScript - Interactive map UI with Google Maps API
- **Backend**: Spring Boot (Java) - REST APIs, route risk scoring, crime data filtering
- **Database**: MySQL - data storage & spatial queries (using Haversine distance calculations)
- **ML**: Pandas, scikit-learn (Python) - KMeans clustering for risk score calculation

### Environment Configuration

#### Database Configuration
- **Unified Configuration**: All applications (Spring Boot, JSP, Python scripts) read database credentials from `db.properties` file.
- **Setup**: Edit `db.properties` and set your MySQL credentials:
  ```properties
  db.host=localhost
  db.port=3306
  db.name=safepath
  db.user=root
  db.password=your_password
  ```
- **Alternative**: Python scripts can also use `.env` file (via `python-dotenv`) as fallback.
- Both `db.properties` and `.env` are ignored by git for security.

#### MySQL Setup
1. Create database: `mysql -u root -p -e "CREATE DATABASE safepath;"`
2. Run schema: `mysql -u root -p safepath < database-manipulation/create_table.sql`
3. Update `db.properties` with your credentials

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

### ✅ Implemented Features

1. **Interactive Crime Map** ✅
   - Display crime markers on Google Maps
   - Click markers to view detailed incident information (type, time, location, precinct, sector, beat, neighborhood, coordinates)
   - Real-time crime data loading based on map bounds
   - Sidebar list with clickable items that navigate to markers

2. **Search & Filter Panel** ✅
   - Filter crimes by type (multi-select, dynamically loaded from database)
   - Filter by time range (24h/7d/30d/90d/custom date range)
   - Auto-apply filters (no need to click "Apply" button)
   - Clear all filters functionality
   - Default shows all crime types if none selected

3. **ETL Data Pipeline** ✅
   - Clean and normalize CSV crime data
   - Unified data schema with proper foreign key relationships
   - Load into MySQL database with spatial indexing
   - Scripts: `etl_spd_crime.py`, `etl_realtime.py`, `geographic_data_processor.py`

4. **Route Risk Scoring** ✅
   - **POST `/api/routes/risk`** - Analyze multiple routes from Google Directions API
   - Maps route points to street segments using 500m matching radius
   - Returns risk scores for each route and step
   - Frontend displays risk scores for route comparison
   - See `ROUTE_RISK_API.md` for detailed API documentation

5. **Safety Recommendations (ML-Based)** ✅
   - KMeans clustering on historical crime data (90-day lookback)
   - Automatic risk score updates on application startup
   - Manual trigger via **POST `/api/risk-score/update`**
   - Status check via **GET `/api/risk-score/status`**
   - Risk scores stored in `street_segment_risk` table

### Planned Features
- Real-time route adjustments based on live incidents
- Mobile app integration
- Multi-city expansion

### Safety Recommendation ML Pipeline

- **Script**: `data-process-insert/segment_risk_clustering.py`
  - Clusters street segments using KMeans (defaults: 90-day lookback, 3 clusters)
  - Reads database credentials from `db.properties` (unified configuration)
  - Run manually: `python data-process-insert/segment_risk_clustering.py`
  
- **Automatic Updates**:
  - Risk scores are automatically updated on Spring Boot application startup
  - Configurable via `application.properties`:
    - `safepath.risk-score.auto-update.enabled=true`
    - `safepath.risk-score.auto-update.delay-seconds=5`
  - Manual trigger: `POST /api/risk-score/update`
  - Status check: `GET /api/risk-score/status`
  - Prevents duplicate concurrent updates and enforces minimum update interval

- **Results**: Persisted to `street_segment_risk` table
  - Fields: `risk_label` (LOW/MEDIUM/HIGH), `risk_score` (0-1), `incident_density`, `night_fraction`, `last_90d_incidents`
  - Schema defined in `database-manipulation/create_table.sql`

- **API Endpoints**:
  - `GET /api/safety/segments/{unitid}` – single segment risk snapshot
  - `GET /api/safety/segments?west=...&south=...&east=...&north=...` – viewport query for maps and routing
  - `POST /api/routes/risk` – route risk analysis (see `ROUTE_RISK_API.md`)

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
- ETL script: `data-process-insert/etl_spd_crime.py`
- Reads CSV files and inserts into MySQL database
- Tables: `crime_reports`, `report_offenses`, `offense_types`
- Clean timestamps and coordinates
- Uses Haversine distance for spatial queries

### 2. Real-Time Emergency Data
**Source:** [Seattle Real-Time Fire 911 Calls](https://data.seattle.gov/Public-Safety/Seattle-Real-Time-Fire-911-Calls/kzjm-xkqj/about_data)

**Processing Method:**
- ETL script: `data-process-insert/etl_realtime.py`
- Poll JSON API periodically
- Insert into `realtime_incidents` table in MySQL

### 3. Street Network Data
**Source:** [Seattle Streets Dataset](https://data-seattlecitygis.opendata.arcgis.com/datasets/SeattleCityGIS::seattle-streets/about)

**Processing Method:**
- Processed via `data-process-insert/geographic_data_processor.py`
- Import into MySQL `street_segments` table
- Stores segment center points (`gis_mid_x`, `gis_mid_y`) for spatial matching

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- MySQL 8.0+
- Python 3.8+ with packages: `mysql-connector-python`, `pandas`, `scikit-learn`, `numpy`, `python-dotenv`
- Google Maps API key

### Setup Steps

1. **Database Setup**
   ```bash
   # Create database
   mysql -u root -p -e "CREATE DATABASE safepath;"
   
   # Run schema
   mysql -u root -p safepath < database-manipulation/create_table.sql
   ```

2. **Configuration**
   - Edit `db.properties` with your MySQL credentials
   - (Optional) Create `.env` file for Python scripts (fallback)

3. **Load Data**
   ```bash
   # Load crime data
   python data-process-insert/etl_spd_crime.py
   
   # Process geographic data
   python data-process-insert/geographic_data_processor.py
   
   # Calculate risk scores
   python data-process-insert/segment_risk_clustering.py
   ```

4. **Run Backend**
   ```bash
   # Spring Boot backend
   mvn spring-boot:run
   # Or: java -jar target/safepath-*.jar
   ```
   Backend runs on `http://localhost:8081`
   
   Note: Risk scores will be automatically updated on startup (configurable in `application.properties`)

5. **Run Frontend**
   ```bash
   # JSP application (using Jetty or Tomcat)
   cd backend-frontend-integretion/safepath_integretion/safepath-jdbc
   mvn jetty:run
   ```
   Frontend runs on `http://localhost:8080` (or configured port)

### API Endpoints

- **Route Risk Analysis**: `POST /api/routes/risk` - See `ROUTE_RISK_API.md`
- **Crime Data Filtering**: `GET /crime-report?action=filter&lat=...&lon=...&radius=...&crimeTypes=...&timeStart=...&timeEnd=...`
- **Crime Types**: `GET /crime-report?action=crimeTypes`
- **Risk Score Update**: `POST /api/risk-score/update`
- **Risk Score Status**: `GET /api/risk-score/status`
- **Safety Segments**: `GET /api/safety/segments/{unitid}` or `GET /api/safety/segments?west=...&south=...&east=...&north=...`

## Technical Details

### Route Matching Algorithm
- Route points are matched to street segments using a **500m bounding box**
- Haversine distance formula is used for precise distance calculation
- Nearest segment is selected for each route point
- Risk scores are aggregated per step and per route
- Matching radius was increased from 100m → 300m → 500m to improve coverage

### Configuration Management
- **Unified Config**: `db.properties` - Single source of truth for database credentials
- **Spring Boot**: Reads from `db.properties` via `spring.config.import`
- **Python Scripts**: Read from `db.properties` via `config_loader.py`, fallback to `.env`
- **JSP Application**: Reads from `db.properties` via `ConnectionManager.java`

### Google Maps API
- Language is set to English (`language=en`) in `index.jsp` and `map.js`
- API key configured in `index.jsp`
- Uses Google Directions API for route planning
- Uses Google Places API for autocomplete

## Troubleshooting

### Risk Scores Show as 0
- Ensure `street_segment_risk` table is populated: `python data-process-insert/segment_risk_clustering.py`
- Check matching radius (currently 500m) in `StreetSegmentRiskRepository.java`
- Verify `report_offenses` table has data linking crimes to segments
- Check backend logs for matching coverage statistics

### No Crime Data Displayed
- Check database connection in `db.properties`
- Verify crime data is loaded: `SELECT COUNT(*) FROM crime_reports;`
- Check frontend console for API errors
- Verify filter settings (default time range is 30 days)

### Google Maps API Issues
- Verify API key is valid
- Check browser console for API errors
- Language is set to English (`language=en`) in `index.jsp` and `map.js`

### Route Matching Issues
- Check backend logs for "Step risk analysis" messages showing matching coverage
- Verify street segments exist in database: `SELECT COUNT(*) FROM street_segments;`
- Ensure risk scores are calculated: `SELECT COUNT(*) FROM street_segment_risk WHERE risk_score > 0;`

## Testing

Run the integration test to verify the Route Risk API:
```bash
mvn test -Dtest=RouteRiskIntegrationTest
```

The test seeds H2 with sample segments and sends a mock Google Directions payload, confirming the response structure and score ranges.

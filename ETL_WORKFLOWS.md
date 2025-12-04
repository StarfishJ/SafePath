# ETL Workflows Documentation

## Overview

SafePath has **4 ETL workflows** that process external data and generate risk scores:

| Workflow | Script | Source | Frequency |
|----------|--------|--------|-----------|
| **1. SPD Crime Data** | `etl_spd_crime.py` | `spd_crime_data.csv` | Daily (24h) |
| **2. Real-time 911** | `etl_realtime.py` | `seattle_realtime_911.csv` | Every 5 min |
| **3. Geographic Data** | `geographic_data_processor.py` | `seattle_streets.csv` | Static |
| **4. Risk Clustering** | `segment_risk_clustering.py` | Combines #1 + #3 | On startup + manual |

---

## Workflow 1: SPD Crime Data ETL

**Input:** CSV file → **Output:** MySQL tables (`crime_reports`, `report_offenses`, `offense_types`)

### Process Flow
```
CSV → Read → Clean → Split → Insert into 3 tables
```

### Components Used

| Category | Component | Purpose |
|----------|-----------|---------|
| **Python Library** | `pandas` | Read CSV, clean data, transform columns |
| **Database** | `mysql-connector-python` | Connect to MySQL, batch inserts |
| **Config** | `config_loader.py` | Read database credentials |
| **Tables** | `sources`, `etl_runs` | Track ETL execution |
| **Tables** | `offense_types`, `crime_reports`, `report_offenses` | Store crime data |

### Key Steps
1. **Extract:** Read CSV with `pd.read_csv()`
2. **Transform:** 
   - Rename columns (e.g., `Report Number` → `report_number`)
   - Parse dates with `pd.to_datetime()`
   - Clean missing values
3. **Load:** Batch insert (1000/batch) into 3 tables:
   - `offense_types` - Crime type master data
   - `crime_reports` - Report records
   - `report_offenses` - Links reports to offense types

---

## Workflow 2: Real-time 911 Events ETL

**Input:** CSV file → **Output:** MySQL table (`realtime_incidents`)

### Process Flow
```
CSV → Read → Validate → Insert
```

### Components Used

| Category | Component | Purpose |
|----------|-----------|---------|
| **Python Library** | `pandas` | Read CSV, validate data |
| **Database** | `mysql-connector-python` | Batch inserts |
| **Config** | `config_loader.py` | Database credentials |
| **Tables** | `sources`, `etl_runs`, `realtime_incidents` | Store incidents |

### Key Steps
1. **Extract:** Read CSV with custom separators
2. **Transform:**
   - Standardize column names (handle variants)
   - Convert dates and coordinates to proper types
   - Filter invalid records (missing ID/coordinates)
3. **Load:** Batch insert (1000/batch) into `realtime_incidents`

---

## Workflow 3: Geographic Data Processing

**Input:** CSV file → **Output:** MySQL tables (`intersections`, `street_segments`)

### Process Flow
```
CSV → Read → Extract intersections → Insert 2 tables
```

### Components Used

| Category | Component | Purpose |
|----------|-----------|---------|
| **Python Library** | `pandas` | Process large CSV, extract intersections |
| **Database** | `mysql-connector-python` | Batch inserts |
| **Config** | `config_loader.py` | Database credentials |
| **Tables** | `intersections`, `street_segments` | Store street network |

### Key Steps
1. **Extract:** Read CSV with `low_memory=False` for large files
2. **Transform:**
   - Extract unique intersections from street endpoints
   - Clean and deduplicate data
3. **Load:** Batch insert (1000/batch) into:
   - `intersections` - Intersection points
   - `street_segments` - Street segments with FK to intersections

---

## Workflow 4: Risk Score Clustering (ML)

**Input:** MySQL tables (`street_segments` + `crime_reports` + `report_offenses`)  
**Output:** MySQL table (`street_segment_risk`)

### Process Flow
```
Query DB → Spatial Match → Feature Engineering → ML Clustering → Update Risk Scores
```

### Components Used

| Category | Component | Purpose |
|----------|-----------|---------|
| **Python Library** | `pandas` | Data manipulation, SQL queries |
| **Python Library** | `numpy` | Numerical operations |
| **ML Library** | `sklearn.neighbors.BallTree` | Spatial nearest-neighbor search (Haversine) |
| **ML Library** | `sklearn.cluster.KMeans` | Clustering into risk categories |
| **ML Library** | `sklearn.preprocessing.StandardScaler` | Normalize features |
| **Database** | `mysql-connector-python` | Query and update |
| **Config** | `config_loader.py` | Database credentials |
| **Algorithm** | Haversine distance | Calculate geographic distances |
| **Tables** | `street_segments`, `crime_reports`, `report_offenses` | Source data |
| **Tables** | `street_segment_risk` | Output risk scores |

### Key Steps

1. **Extract:**
   - Query `street_segments` (geometry)
   - Query `crime_reports` + `report_offenses` (last 90 days)

2. **Spatial Matching:**
   - Use `BallTree` with Haversine metric
   - Match each crime incident to nearest street segment

3. **Feature Engineering:**
   - **Incident Density:** `incidents_90d / segment_length`
   - **Night Fraction:** `night_incidents / total_incidents` (22:00-05:59)
   - **Trend Ratio:** `(recent_30d + 1) / (previous_60d + 1)`

4. **ML Clustering:**
   - Normalize features with `StandardScaler`
   - Apply `KMeans` (3 clusters)
   - Map clusters to labels: LOW, MEDIUM, HIGH
   - Calculate risk score (0-1 normalized)

5. **Load:**
   - Batch UPSERT (100/batch) into `street_segment_risk`
   - Store: `risk_label`, `risk_score`, `incident_density`, etc.

### Configuration
- `LOOKBACK_DAYS=90` - How far back to analyze
- `RECENT_WINDOW_DAYS=30` - Recent vs previous period
- `N_CLUSTERS=3` - Number of risk categories
- `BATCH_SIZE=100` - Update batch size

### Auto-Execution
- **Spring Boot:** Auto-runs on startup (configurable)
- **API:** Manual trigger via `POST /api/risk-score/update`
- **Min Interval:** 1 hour between updates

---

## Workflow Dependencies

```
Workflow 3 (Geographic) ──┐
                          ├─→ Must run first
Workflow 1 (Crime Data) ──┤
                          │
Workflow 4 (Risk Clustering) ──→ Depends on #1 and #3
```

**Execution Order:**
1. Run Workflow 3 (Geographic) - **Required first**
2. Run Workflow 1 (Crime Data) - **Required for risk scoring**
3. Run Workflow 4 (Risk Clustering) - **Generates risk scores**
4. Run Workflow 2 (Real-time) - **Independent, for live incidents**

---

## Technology Stack Summary

| Category | Technologies |
|----------|-------------|
| **Python Libraries** | pandas, numpy, scikit-learn, mysql-connector-python |
| **Database** | MySQL 8.0+ (InnoDB, UTF8MB4) |
| **ML Algorithms** | KMeans clustering, BallTree spatial search, Haversine distance |
| **Configuration** | `db.properties` (primary), `.env` (fallback) |
| **Automation** | Spring Boot (Java) for auto-execution |

---

## Quick Reference

### Run ETL Scripts
```bash
# 1. Geographic data (run first)
python data-process-insert/geographic_data_processor.py

# 2. Crime data
python data-process-insert/etl_spd_crime.py

# 3. Risk clustering
python data-process-insert/segment_risk_clustering.py

# 4. Real-time 911 (optional, for live incidents)
python data-process-insert/etl_realtime.py
```

### Database Tables Created
- `sources` - Data source registry
- `etl_runs` - ETL execution tracking
- `offense_types` - Crime type master data
- `crime_reports` - Crime report records
- `report_offenses` - Report-offense relationships
- `realtime_incidents` - Real-time 911 incidents
- `intersections` - Street intersection points
- `street_segments` - Street segment geometry
- `street_segment_risk` - Computed risk scores (from ML workflow)

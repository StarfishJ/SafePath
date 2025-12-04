import os
import pandas as pd
import mysql.connector
from datetime import datetime
import time

# use the unified config loader to read the database configuration
# from the db.properties file
try:
    from config_loader import get_db_config
    db_config = get_db_config()
    DB_HOST = db_config.get("DB_HOST", "localhost")
    DB_USER = db_config.get("DB_USER", "root")
    DB_PASSWORD = db_config.get("DB_PASSWORD", "")
    DB_NAME = db_config.get("DB_NAME", "safepath")
except ImportError:
    # fallback to using environment variables if the config_loader
    # is not available
    from dotenv import load_dotenv
    load_dotenv()
    DB_HOST = os.getenv("DB_HOST", "localhost")
    DB_USER = os.getenv("DB_USER", "root")
    DB_PASSWORD = os.getenv("DB_PASSWORD", "")
    DB_NAME = os.getenv("DB_NAME", "safepath")

start_time = time.time()

# =======================
# 1️⃣ Connecting to MySQL
# =======================
conn = mysql.connector.connect(
    host=DB_HOST,
    user=DB_USER,
    password=DB_PASSWORD,
    database=DB_NAME,
)
cursor = conn.cursor()

# =======================
# 2️⃣ Initialize Source
# =======================
source_name = "Seattle Fire Real-Time 911"
source_url = "https://data.seattle.gov/resource/kzjm-xkqj.csv"
refresh_interval = 5

cursor.execute(
    "SELECT source_id FROM sources WHERE source_name=%s", (source_name,))
row = cursor.fetchone()
if row:
    source_id = row[0]
else:
    cursor.execute(
        "INSERT INTO sources (source_name, url, refresh_interval) \
        VALUES (%s, %s, %s)",
        (source_name, source_url, refresh_interval),
    )
    conn.commit()
    source_id = cursor.lastrowid
print(f"Source ID = {source_id}")

# =======================
# 3️⃣ Create ETL_Run
# =======================
cursor.execute(
    "INSERT INTO etl_runs (source_id, run_time, record_count) \
        VALUES (%s, %s, 0)",
    (source_id, datetime.now()),
)
conn.commit()
etl_id = cursor.lastrowid
print(f"ETL Run ID = {etl_id}")

# =======================
# 4️⃣ Reading Data
# =======================
print("Loading CSV...")

df = pd.read_csv("../data/seattle_realtime_911.csv", sep=",", quotechar='"')

df.columns = df.columns.str.strip()
print("Columns:", list(df.columns))

rename_map = {
    "Incident Number": "incident_id",
    "IncidentNumber": "incident_id",
    "Type": "incident_type",
    "Datetime": "event_datetime",
    "DateTime": "event_datetime",
    "Address": "address",
    "Latitude": "latitude",
    "Longitude": "longitude",
    "Report Location": "report_location",
    "ReportLocation": "report_location",
}
df = df.rename(columns=rename_map)

# =======================
# 5️⃣ Cleaning Field
# =======================
if "event_datetime" not in df.columns:
    raise ValueError(
        f"The event_datetime column does not exist, the current column name\
        is: {list(df.columns)}"
    )

df["event_datetime"] = pd.to_datetime(
    df["event_datetime"], format="%Y %b %d %I:%M:%S %p", errors="coerce"
)
df["latitude"] = pd.to_numeric(df["latitude"], errors="coerce")
df["longitude"] = pd.to_numeric(df["longitude"], errors="coerce")

df = df.dropna(subset=["incident_id", "latitude", "longitude"])
df = df.where(pd.notnull(df), None)

print(f"{len(df)} valid rows loaded.")

# =======================
# 6️⃣ INSERT INTO realtime_incidents
# =======================
print("Inserting realtime_incidents...")

records = [
    (
        row["incident_id"],
        row["incident_type"],
        row["event_datetime"],
        row["address"],
        row["latitude"],
        row["longitude"],
        row["report_location"],
        source_id,
    )
    for _, row in df.iterrows()
]

batch_size = 1000
for i in range(0, len(records), batch_size):
    batch = records[i: i + batch_size]
    cursor.executemany(
        """
        INSERT IGNORE INTO realtime_incidents
        (incident_id, incident_type, event_datetime, address,
         latitude, longitude, report_location, source_id)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
        """,
        batch,
    )
    conn.commit()

    if i % 10000 == 0:
        print(f"   Progress: {i + len(batch)} / {len(records)} rows inserted")

print(f"Inserted {len(df)} realtime incidents.")

# =======================
# 7️⃣ Update ETL_Run Record
# =======================
cursor.execute(
    "UPDATE etl_runs SET record_count = %s WHERE etl_id = %s", (len(
        df), etl_id)
)
conn.commit()

# =======================
# 8️⃣ End
# =======================
cursor.close()
conn.close()

print(f"ETL completed in {time.time() - start_time:.2f}s.")

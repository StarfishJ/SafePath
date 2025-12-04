import os
from datetime import datetime
import mysql.connector
import pandas as pd

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

# ============================
# 1️⃣ MySQL connection configuration
# ============================
conn = mysql.connector.connect(
    host=DB_HOST,
    user=DB_USER,
    password=DB_PASSWORD,
    database=DB_NAME,
)
cursor = conn.cursor()

# =======================
# Initialize Source
# =======================
source_name = "SPD Crime Data"
source_url = "https://data.seattle.gov/Public-Safety/SPD-Crime-Data-2008-Present"
refresh_interval = 1440

cursor.execute(
    "SELECT source_id FROM sources WHERE source_name=%s", (source_name,))
row = cursor.fetchone()
if row:
    source_id = row[0]
else:
    cursor.execute(
        "INSERT INTO sources (source_name, url, refresh_interval) VALUES (%s, %s, %s)",
        (source_name, source_url, refresh_interval),
    )
    conn.commit()
    source_id = cursor.lastrowid
print(f"Source ID = {source_id}")

# =======================
# Create ETL_Run
# =======================
cursor.execute(
    "INSERT INTO etl_runs (source_id, run_time, record_count) VALUES (%s, %s, 0)",
    (source_id, datetime.now()),
)
conn.commit()
etl_id = cursor.lastrowid
print(f"ETL Run ID = {etl_id}")

# ============================
# 2️⃣ Reading CSV
# ============================
print("Loading CSV...")
df = pd.read_csv("../data/spd_crime_data.csv")

# ============================
# 3️⃣ Clean fields & rename
# ============================
df = df.rename(
    columns={
        "Report Number": "report_number",
        "Report DateTime": "report_datetime",
        "Offense ID": "offense_id",
        "Offense Date": "offense_date",
        "NIBRS Group AB": "group_a_b",
        "NIBRS Crime Against Category": "crime_against_category",
        "Offense Sub Category": "offense_sub_category",
        "Offense Category": "offense_parent_group",
        "NIBRS Offense Code Description": "offense_name",
        "NIBRS_offense_code": "offense_code",
        "Block Address": "blurred_address",
        "Latitude": "blurred_latitude",
        "Longitude": "blurred_longitude",
        "Precinct": "precinct",
        "Sector": "sector",
        "Beat": "beat",
        "Neighborhood": "mcpp_neighborhood",
    }
)

# Convert date format
df["report_datetime"] = pd.to_datetime(
    df["report_datetime"], format="%Y %b %d %I:%M:%S %p", errors="coerce"
)

df["offense_date"] = pd.to_datetime(
    df["offense_date"], format="%Y %b %d %I:%M:%S %p", errors="coerce"
)

# Convert all NaNs to None, letting the database insert NULL
df = df.where(pd.notnull(df), None)

# Add etl_id
df["etl_id"] = etl_id

# ============================
# 4️⃣ To regenerate offense_types
# ============================
print("Inserting offense_types...")
offense_types = df[
    [
        "offense_code",
        "offense_name",
        "offense_parent_group",
        "group_a_b",
        "crime_against_category",
    ]
].drop_duplicates()

offense_data = [tuple(row) for row in offense_types.to_numpy()]

# Batch insert offense_types
cursor.executemany(
    """
    INSERT IGNORE INTO offense_types
    (offense_code, offense_name, offense_parent_group, group_a_b, crime_against_category)
    VALUES (%s, %s, %s, %s, %s)
    """,
    offense_data,
)
conn.commit()
print(f"Inserted {len(offense_data)} offense types.")

# ============================
# 5️⃣ INSERT INTO crime_reports
# ============================
print("Inserting crime_reports...")
crime_reports = df[
    [
        "report_number",
        "report_datetime",
        "precinct",
        "sector",
        "beat",
        "mcpp_neighborhood",
        "blurred_address",
        "blurred_latitude",
        "blurred_longitude",
        "etl_id",
    ]
].dropna(subset=["report_number"])  # Remove report_number that is empty

crime_data = [tuple(row) for row in crime_reports.to_numpy()]

batch_size = 1000
for i in range(0, len(crime_data), batch_size):
    batch = crime_data[i: i + batch_size]
    cursor.executemany(
        """
        INSERT IGNORE INTO crime_reports
        (report_number, report_datetime, precinct, sector, beat,
         mcpp_neighborhood, blurred_address, blurred_latitude,
         blurred_longitude, etl_id)
        VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)
        """,
        batch,
    )
    conn.commit()
    if i % 10000 == 0:
        print(f"   Progress: {i + len(batch)} / {len(crime_data)} rows")

print(f"Inserted {len(crime_data)} crime reports.")

# ============================
# 6️⃣ INSERT INTO report_offenses
# ============================
print("Inserting report_offenses...")

valid_reports = set(crime_reports["report_number"].unique())

report_offenses = df[
    ["report_number", "offense_code", "offense_date", "etl_id"]
].dropna(subset=["report_number"])

report_offenses = report_offenses[report_offenses["report_number"].isin(
    valid_reports)]

report_data = [tuple(row) for row in report_offenses.to_numpy()]

for i in range(0, len(report_data), batch_size):
    batch = report_data[i: i + batch_size]
    cursor.executemany(
        """
        INSERT INTO report_offenses
        (report_number, offense_code, offense_date, etl_id)
        VALUES (%s, %s, %s, %s)
        """,
        batch,
    )
    conn.commit()
    if i % 10000 == 0:
        print(f"   Progress: {i + len(batch)} / {len(report_data)} rows")

print(f"Inserted {len(report_data)} report offenses.")

# ============================
# 7️⃣ Update etl_runs record count
# ============================
cursor.execute(
    """
    UPDATE etl_runs
    SET record_count = %s
    WHERE etl_id = %s
""",
    (len(df), etl_id),
)
conn.commit()

cursor.close()
conn.close()

print(f"ETL completed for SPD Crime Data — {len(df)} records processed.")

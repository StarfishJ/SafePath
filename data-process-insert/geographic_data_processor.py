import pandas as pd
import mysql.connector
import time

start = time.time()
print("Loading Seattle streets data...")

# 1️⃣ Connecting to MySQL
conn = mysql.connector.connect(
    host="localhost", user="root", password="zcc663280", database="safepath"
)
cursor = conn.cursor()

# 2️⃣ Reading CSV
df = pd.read_csv("../data/seattle_streets.csv", sep=",", low_memory=False)
df = df.where(pd.notnull(df), None)
print(f"Loaded {len(df)} raw rows.")

# 3️⃣ Cleaning
df = df.dropna(subset=["UNITID", "INTKEYLO", "INTKEYHI"])
df = df.where(pd.notnull(df), None)

# 4️⃣ Extract intersections
intersections = pd.concat(
    [
        df[["INTKEYLO", "INTRLO", "DIRLO", "GIS_MID_X", "GIS_MID_Y"]].rename(
            columns={
                "INTKEYLO": "intkey",
                "INTRLO": "name",
                "DIRLO": "direction",
                "GIS_MID_X": "gis_x",
                "GIS_MID_Y": "gis_y",
            }
        ),
        df[["INTKEYHI", "INTRHI", "DIRHI", "GIS_MID_X", "GIS_MID_Y"]].rename(
            columns={
                "INTKEYHI": "intkey",
                "INTRHI": "name",
                "DIRHI": "direction",
                "GIS_MID_X": "gis_x",
                "GIS_MID_Y": "gis_y",
            }
        ),
    ]
).drop_duplicates(subset=["intkey"])

print(f"Extracted {len(intersections)} unique intersections.")

# 5️⃣ Batch insert intersections
batch_size = 1000
intersections = intersections.where(pd.notnull(intersections), None)
records = [
    tuple(None if (isinstance(v, float) and pd.isna(v)) else v for v in row)
    for row in intersections.itertuples(index=False, name=None)
]
for i in range(0, len(records), batch_size):
    batch = records[i : i + batch_size]
    cursor.executemany(
        """
        INSERT IGNORE INTO intersections (intkey, name, direction, gis_x, gis_y)
        VALUES (%s, %s, %s, %s, %s)
    """,
        batch,
    )
    conn.commit()
    if i % 10000 == 0:
        print(f"   Progress: {i + len(batch)} / {len(records)} intersections")

# 6️⃣ Insert street_segments
segments = df[
    [
        "UNITID",
        "ONSTREET",
        "SPEEDLIMIT",
        "ARTCLASS",
        "STATUS",
        "SEGLENGTH",
        "SURFACEWIDTH",
        "SLOPE_PCT",
        "OWNER",
        "ONEWAY",
        "FLOW",
        "GIS_MID_X",
        "GIS_MID_Y",
        "INTKEYLO",
        "INTKEYHI",
    ]
].drop_duplicates(subset=["UNITID"])

segments = segments.where(pd.notnull(segments), None)

segment_records = [
    tuple(None if (isinstance(v, float) and pd.isna(v)) else v for v in row)
    for row in segments.itertuples(index=False, name=None)
]

print(f"Ready to insert {len(segment_records)} street segments...")

for i in range(0, len(segment_records), batch_size):
    batch = segment_records[i : i + batch_size]
    cursor.executemany(
        """
        INSERT IGNORE INTO street_segments (
            unitid, onstreet, speedlimit, artclass, status,
            seglength, surfacewidth, slope_pct, owner,
            oneway, flow, gis_mid_x, gis_mid_y, start_intkey, end_intkey
        ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
    """,
        batch,
    )
    conn.commit()
    if i % 10000 == 0:
        print(f"   Progress: {i + len(batch)} / {len(segment_records)} street segments")

cursor.close()
conn.close()
print(f"Completed in {time.time() - start:.2f}s.")

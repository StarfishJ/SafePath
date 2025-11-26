"""
Compute safety risk clusters for Seattle street segments using KMeans.

This script reads street geometry and recent crime incidents from MySQL,
engineers per-segment features, clusters segments into three risk groups,
and stores the results back into the `street_segment_risk` table.
"""

from __future__ import annotations

import logging
import os
from dataclasses import dataclass
from datetime import datetime, timedelta, timezone
from typing import List

import mysql.connector
import numpy as np
import pandas as pd
from mysql.connector import MySQLConnection
from sklearn.cluster import KMeans
from sklearn.neighbors import BallTree
from sklearn.preprocessing import StandardScaler
from dotenv import load_dotenv

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
)
LOGGER = logging.getLogger("segment_risk_clustering")

load_dotenv()


# ---------------------------------------------------------------------------
# Configuration
# ---------------------------------------------------------------------------


@dataclass
class DbConfig:
    host: str
    port: int
    user: str
    password: str
    database: str

    @classmethod
    def from_env(cls) -> "DbConfig":
        return cls(
            host=os.getenv("DB_HOST", "localhost"),
            port=int(os.getenv("DB_PORT", "3306")),
            user=os.getenv("DB_USER", "root"),
            password=os.getenv("DB_PASSWORD", "zcc663280"),
            database=os.getenv("DB_NAME", "safepath"),
        )


MODEL_VERSION = os.getenv("SEGMENT_RISK_MODEL_VERSION", "kmeans_c1_v1")
LOOKBACK_DAYS = int(os.getenv("SEGMENT_RISK_LOOKBACK_DAYS", "90"))
RECENT_WINDOW_DAYS = int(os.getenv("SEGMENT_RISK_RECENT_DAYS", "30"))
N_CLUSTERS = int(os.getenv("SEGMENT_RISK_NUM_CLUSTERS", "3"))
EARTH_RADIUS_METERS = 6_371_000


# ---------------------------------------------------------------------------
# Database helpers
# ---------------------------------------------------------------------------


def get_connection(cfg: DbConfig) -> MySQLConnection:
    return mysql.connector.connect(
        host=cfg.host,
        port=cfg.port,
        user=cfg.user,
        password=cfg.password,
        database=cfg.database,
    )


CREATE_TABLE_SQL = """
CREATE TABLE IF NOT EXISTS street_segment_risk (
    unitid VARCHAR(50) PRIMARY KEY,
    cluster_id INT NOT NULL,
    risk_label VARCHAR(10) NOT NULL,
    risk_score DOUBLE NOT NULL,
    incident_density DOUBLE,
    night_fraction DOUBLE,
    last_90d_incidents INT,
    model_version VARCHAR(20),
    override_reason VARCHAR(255),
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (unitid) REFERENCES street_segments(unitid)
        ON UPDATE CASCADE ON DELETE CASCADE
)
"""


UPSERT_SQL = """
INSERT INTO street_segment_risk (
    unitid,
    cluster_id,
    risk_label,
    risk_score,
    incident_density,
    night_fraction,
    last_90d_incidents,
    model_version,
    override_reason,
    updated_at
)
VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
ON DUPLICATE KEY UPDATE
    cluster_id = VALUES(cluster_id),
    risk_label = VALUES(risk_label),
    risk_score = VALUES(risk_score),
    incident_density = VALUES(incident_density),
    night_fraction = VALUES(night_fraction),
    last_90d_incidents = VALUES(last_90d_incidents),
    model_version = VALUES(model_version),
    override_reason = VALUES(override_reason),
    updated_at = VALUES(updated_at)
"""


# ---------------------------------------------------------------------------
# Data extraction
# ---------------------------------------------------------------------------


def fetch_segments(conn: MySQLConnection) -> pd.DataFrame:
    query = """
    SELECT
        unitid,
        onstreet,
        seglength,
        gis_mid_x AS longitude,
        gis_mid_y AS latitude
    FROM street_segments
    WHERE gis_mid_x IS NOT NULL AND gis_mid_y IS NOT NULL
    """
    df = pd.read_sql(query, conn)
    df = df.dropna(subset=["unitid"]).reset_index(drop=True)
    return df


def fetch_incidents(conn: MySQLConnection, window_start: datetime) -> pd.DataFrame:
    query = """
    SELECT
        cr.report_number,
        ro.offense_date,
        cr.blurred_latitude AS latitude,
        cr.blurred_longitude AS longitude
    FROM report_offenses ro
    INNER JOIN crime_reports cr ON ro.report_number = cr.report_number
    WHERE cr.blurred_latitude IS NOT NULL
      AND cr.blurred_longitude IS NOT NULL
      AND ro.offense_date >= %s
    """
    df = pd.read_sql(
        query,
        conn,
        params=[window_start],
        parse_dates=["offense_date"],
    )
    df = df.dropna(subset=["latitude", "longitude",
                   "offense_date"]).reset_index(drop=True)
    return df


# ---------------------------------------------------------------------------
# Feature engineering
# ---------------------------------------------------------------------------


def assign_incidents_to_segments(
    segments: pd.DataFrame, incidents: pd.DataFrame
) -> pd.DataFrame:
    if incidents.empty or segments.empty:
        return pd.DataFrame(
            columns=[
                "unitid",
                "is_night",
                "is_recent",
                "is_previous",
            ]
        )

    segment_coords = np.radians(
        segments[["latitude", "longitude"]].to_numpy(dtype=float))
    incident_coords = np.radians(
        incidents[["latitude", "longitude"]].to_numpy(dtype=float)
    )

    tree = BallTree(segment_coords, metric="haversine")
    distances, indices = tree.query(incident_coords, k=1)

    incident_times = incidents["offense_date"]
    night_mask = incident_times.dt.hour.isin([22, 23, 0, 1, 2, 3, 4, 5])

    now = datetime.utcnow()
    recent_cutoff = now - timedelta(days=RECENT_WINDOW_DAYS)
    previous_cutoff = now - timedelta(days=LOOKBACK_DAYS)

    is_recent = incident_times >= recent_cutoff
    is_previous = (incident_times >= previous_cutoff) & (
        incident_times < recent_cutoff)

    assignment = pd.DataFrame(
        {
            "unitid": segments.loc[indices.flatten(), "unitid"].to_numpy(),
            "is_night": night_mask.to_numpy(dtype=int),
            "is_recent": is_recent.to_numpy(dtype=int),
            "is_previous": is_previous.to_numpy(dtype=int),
        }
    )
    return assignment


def build_feature_frame(
    segments: pd.DataFrame, assignments: pd.DataFrame
) -> pd.DataFrame:
    agg = assignments.groupby("unitid").agg(
        incidents_90d=("unitid", "size"),
        night_incidents=("is_night", "sum"),
        recent_incidents=("is_recent", "sum"),
        previous_incidents=("is_previous", "sum"),
    )
    features = segments.merge(
        agg, how="left", left_on="unitid", right_index=True)
    for col in ["incidents_90d", "night_incidents", "recent_incidents", "previous_incidents"]:
        features[col] = features[col].fillna(0.0)

    # Avoid divide-by-zero issues
    features["effective_length"] = features["seglength"].fillna(
        100.0).clip(lower=50.0)
    features["incident_density"] = (
        features["incidents_90d"] / features["effective_length"]
    )

    features["night_fraction"] = np.where(
        features["incidents_90d"] > 0,
        features["night_incidents"] / features["incidents_90d"],
        0.0,
    )

    features["trend_ratio"] = (
        (features["recent_incidents"] + 1.0)
        / (features["previous_incidents"] + 1.0)
    )

    return features


# ---------------------------------------------------------------------------
# Clustering and risk mapping
# ---------------------------------------------------------------------------


def cluster_segments(features: pd.DataFrame) -> pd.DataFrame:
    feature_cols = ["incident_density", "night_fraction", "trend_ratio"]
    matrix = features[feature_cols].to_numpy(dtype=float)

    scaler = StandardScaler()
    scaled = scaler.fit_transform(matrix)

    model = KMeans(
        n_clusters=N_CLUSTERS,
        n_init=10,
        random_state=42,
    )
    clusters = model.fit_predict(scaled)
    features = features.copy()
    features["cluster_id"] = clusters

    density_means = (
        features.groupby("cluster_id")["incident_density"].mean().sort_values()
    )
    ordered_cluster_ids: List[int] = density_means.index.tolist()

    label_order = ["LOW", "MEDIUM", "HIGH"]
    if len(label_order) != N_CLUSTERS:
        label_order = ["LOW", "MEDIUM", "HIGH", "VERY_HIGH"][:N_CLUSTERS]

    cluster_to_label = {
        cluster_id: label_order[min(idx, len(label_order) - 1)]
        for idx, cluster_id in enumerate(ordered_cluster_ids)
    }

    features["risk_label"] = features["cluster_id"].map(cluster_to_label)

    density_min = features["incident_density"].min()
    density_max = features["incident_density"].max()
    if density_max - density_min < 1e-9:
        features["risk_score"] = 0.0
    else:
        features["risk_score"] = (
            (features["incident_density"] - density_min) /
            (density_max - density_min)
        )

    features["summary"] = features.apply(
        lambda row: (
            f"{int(row['incidents_90d'])} incidents in {LOOKBACK_DAYS}d, "
            f"night {row['night_fraction']:.0%}, trend x{row['trend_ratio']:.2f}"
        ),
        axis=1,
    )

    return features


# ---------------------------------------------------------------------------
# Persistence
# ---------------------------------------------------------------------------


def persist_results(conn: MySQLConnection, features: pd.DataFrame) -> None:
    now = datetime.now(timezone.utc)
    rows = [
        (
            row.unitid,
            int(row.cluster_id),
            row.risk_label,
            float(row.risk_score),
            float(row.incident_density),
            float(row.night_fraction),
            int(row.incidents_90d),
            MODEL_VERSION,
            row.summary,
            now,
        )
        for row in features.itertuples(index=False)
    ]
    cursor = conn.cursor()
    cursor.execute(CREATE_TABLE_SQL)
    cursor.executemany(UPSERT_SQL, rows)
    conn.commit()
    cursor.close()


# ---------------------------------------------------------------------------
# Main entry point
# ---------------------------------------------------------------------------


def main() -> None:
    cfg = DbConfig.from_env()
    LOGGER.info(
        "Starting segment risk clustering (lookback=%sd, clusters=%s, model=%s)",
        LOOKBACK_DAYS,
        N_CLUSTERS,
        MODEL_VERSION,
    )

    with get_connection(cfg) as conn:
        window_start = datetime.now(
            timezone.utc) - timedelta(days=LOOKBACK_DAYS)
        segments = fetch_segments(conn)
        LOGGER.info("Loaded %s street segments.", len(segments))

        incidents = fetch_incidents(conn, window_start)
        LOGGER.info("Loaded %s crime incidents since %s.",
                    len(incidents), window_start.date())

        assignments = assign_incidents_to_segments(segments, incidents)
        LOGGER.info("Mapped incidents to %s segments.",
                    assignments["unitid"].nunique())

        feature_frame = build_feature_frame(segments, assignments)
        if feature_frame.empty:
            LOGGER.warning("No features computed; skipping clustering.")
            return

        clustered = cluster_segments(feature_frame)
        persist_results(conn, clustered)
        LOGGER.info("Persisted risk scores for %s segments.", len(clustered))


if __name__ == "__main__":
    main()

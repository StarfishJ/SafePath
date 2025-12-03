package com.safepath.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.safepath.model.StreetSegmentRisk;

@Repository
public interface StreetSegmentRiskRepository extends JpaRepository<StreetSegmentRisk, String> {

    @Query("SELECT r FROM StreetSegmentRisk r "
            + "JOIN FETCH r.streetSegment s "
            + "WHERE r.unitid = :unitid")
    Optional<StreetSegmentRisk> findByUnitidWithSegment(@Param("unitid") String unitid);

    @Query("SELECT r FROM StreetSegmentRisk r "
            + "JOIN FETCH r.streetSegment s "
            + "WHERE s.gisMidX BETWEEN :west AND :east "
            + "AND s.gisMidY BETWEEN :south AND :north")
    List<StreetSegmentRisk> findWithinBounds(
            @Param("west") double west,
            @Param("south") double south,
            @Param("east") double east,
            @Param("north") double north);

    /**
     * Finds the nearest street segment to a given coordinate using Haversine
     * distance.
     * Used by route risk scoring to map polyline points to street segments.
     *
     * @param lat latitude of the point
     * @param lon longitude of the point
     * @return the closest StreetSegmentRisk within ~500m bounding box, or empty if
     *         none found
     *         
     * Note: Increased bounding box from 0.003 (300m) to 0.005 (500m) to improve
     *       matching coverage, especially for routes that may not align perfectly
     *       with street segment center points.
     */
    @Query(value = "SELECT r.* FROM street_segment_risk r "
            + "JOIN street_segments s ON r.unitid = s.unitid "
            + "WHERE s.gis_mid_x BETWEEN :lon - 0.005 AND :lon + 0.005 "
            + "AND s.gis_mid_y BETWEEN :lat - 0.005 AND :lat + 0.005 "
            + "ORDER BY ("
            + "  6371000 * ACOS(" // Haversine formula in meters
            + "    COS(RADIANS(:lat)) * COS(RADIANS(s.gis_mid_y)) * "
            + "    COS(RADIANS(s.gis_mid_x) - RADIANS(:lon)) + "
            + "    SIN(RADIANS(:lat)) * SIN(RADIANS(s.gis_mid_y))"
            + "  )"
            + ") LIMIT 1", nativeQuery = true)
    Optional<StreetSegmentRisk> findNearestSegment(@Param("lat") double lat, @Param("lon") double lon);
}

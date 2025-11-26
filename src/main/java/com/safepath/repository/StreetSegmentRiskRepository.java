package com.safepath.repository;

import com.safepath.model.StreetSegmentRisk;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StreetSegmentRiskRepository extends JpaRepository<StreetSegmentRisk, String> {

    @Query(
        "SELECT r FROM StreetSegmentRisk r "
            + "JOIN FETCH r.streetSegment s "
            + "WHERE r.unitid = :unitid"
    )
    Optional<StreetSegmentRisk> findByUnitidWithSegment(@Param("unitid") String unitid);

    @Query(
        "SELECT r FROM StreetSegmentRisk r "
            + "JOIN FETCH r.streetSegment s "
            + "WHERE s.gisMidX BETWEEN :west AND :east "
            + "AND s.gisMidY BETWEEN :south AND :north"
    )
    List<StreetSegmentRisk> findWithinBounds(
        @Param("west") double west,
        @Param("south") double south,
        @Param("east") double east,
        @Param("north") double north
    );
}


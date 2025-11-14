package com.safepath.repository;

import com.safepath.model.StreetSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StreetSegmentRepository extends JpaRepository<StreetSegment, String> {
}

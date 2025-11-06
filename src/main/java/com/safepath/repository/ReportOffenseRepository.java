package com.safepath.repository;

import com.safepath.model.ReportOffense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportOffenseRepository extends JpaRepository<ReportOffense, Long> {
}

package com.safepath.repository;

import com.safepath.model.CrimeReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrimeReportRepository extends JpaRepository<CrimeReport, String> {
}

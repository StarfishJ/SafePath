package com.safepath.service;

import com.safepath.model.CrimeReport;

import java.util.List;
import java.util.Optional;

public interface CrimeReportService {
    List<CrimeReport> findAll();

    Optional<CrimeReport> findById(String reportNumber);

    CrimeReport save(CrimeReport crimeReport);

    void deleteById(String reportNumber);
}

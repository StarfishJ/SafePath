package com.safepath.service.impl;

import com.safepath.model.CrimeReport;
import com.safepath.repository.CrimeReportRepository;
import com.safepath.service.CrimeReportService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CrimeReportServiceImpl implements CrimeReportService {

    private final CrimeReportRepository repository;

    public CrimeReportServiceImpl(CrimeReportRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<CrimeReport> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<CrimeReport> findById(String reportNumber) {
        return repository.findById(reportNumber);
    }

    @Override
    public CrimeReport save(CrimeReport crimeReport) {
        return repository.save(crimeReport);
    }

    @Override
    public void deleteById(String reportNumber) {
        repository.deleteById(reportNumber);
    }
}

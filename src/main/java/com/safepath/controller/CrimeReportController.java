package com.safepath.controller;

import com.safepath.dto.CreateCrimeReportRequest;
import com.safepath.dto.CrimeReportDto;
import com.safepath.model.CrimeReport;
import com.safepath.service.CrimeReportService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/crime-reports")
public class CrimeReportController {

    private final CrimeReportService service;

    public CrimeReportController(CrimeReportService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<CrimeReportDto>> list() {
        List<CrimeReport> list = service.findAll();
        List<CrimeReportDto> dtoList = list.stream().map(this::toDto).toList();
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CrimeReportDto> get(@PathVariable("id") String id) {
        return service.findById(id).map(cr -> ResponseEntity.ok(toDto(cr))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CrimeReportDto> create(@Valid @RequestBody CreateCrimeReportRequest req) {
        CrimeReport cr = new CrimeReport();
        cr.setReportNumber(req.getReportNumber());
        cr.setReportDatetime(req.getReportDatetime());
        cr.setPrecinct(req.getPrecinct());
        cr.setSector(req.getSector());
        cr.setBeat(req.getBeat());
        cr.setMcppNeighborhood(req.getMcppNeighborhood());
        cr.setBlurredAddress(req.getBlurredAddress());
        cr.setBlurredLatitude(req.getBlurredLatitude());
        cr.setBlurredLongitude(req.getBlurredLongitude());

        CrimeReport saved = service.save(cr);
        return ResponseEntity.created(URI.create("/api/crime-reports/" + saved.getReportNumber())).body(toDto(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private CrimeReportDto toDto(CrimeReport c) {
        if (c == null) return null;
        CrimeReportDto d = new CrimeReportDto();
        d.setReportNumber(c.getReportNumber());
        d.setReportDatetime(c.getReportDatetime());
        d.setPrecinct(c.getPrecinct());
        d.setSector(c.getSector());
        d.setBeat(c.getBeat());
        d.setMcppNeighborhood(c.getMcppNeighborhood());
        d.setBlurredAddress(c.getBlurredAddress());
        d.setBlurredLatitude(c.getBlurredLatitude());
        d.setBlurredLongitude(c.getBlurredLongitude());
        return d;
    }
}

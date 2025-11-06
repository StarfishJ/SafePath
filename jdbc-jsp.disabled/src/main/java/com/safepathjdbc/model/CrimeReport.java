package com.safepathjdbc.model;

import java.time.LocalDateTime;

public class CrimeReport {
    private Long id;
    private String description;
    private String location;
    private LocalDateTime reportedAt;

    public CrimeReport() {}

    public CrimeReport(Long id, String description, String location, LocalDateTime reportedAt) {
        this.id = id;
        this.description = description;
        this.location = location;
        this.reportedAt = reportedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDateTime getReportedAt() { return reportedAt; }
    public void setReportedAt(LocalDateTime reportedAt) { this.reportedAt = reportedAt; }
}

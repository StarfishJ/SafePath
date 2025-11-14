package com.safepathjdbc.model;

import java.time.LocalDateTime;

public class CrimeReport {
    // Align to Spring entity (crime_reports table)
    private String reportNumber;
    private LocalDateTime reportDatetime;
    private String precinct;
    private String sector;
    private String beat;
    private String mcppNeighborhood;
    private String blurredAddress;
    private Double blurredLatitude;
    private Double blurredLongitude;

    public CrimeReport() {}

    public String getReportNumber() { return reportNumber; }
    public void setReportNumber(String reportNumber) { this.reportNumber = reportNumber; }

    public LocalDateTime getReportDatetime() { return reportDatetime; }
    public void setReportDatetime(LocalDateTime reportDatetime) { this.reportDatetime = reportDatetime; }

    public String getPrecinct() { return precinct; }
    public void setPrecinct(String precinct) { this.precinct = precinct; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public String getBeat() { return beat; }
    public void setBeat(String beat) { this.beat = beat; }

    public String getMcppNeighborhood() { return mcppNeighborhood; }
    public void setMcppNeighborhood(String mcppNeighborhood) { this.mcppNeighborhood = mcppNeighborhood; }

    public String getBlurredAddress() { return blurredAddress; }
    public void setBlurredAddress(String blurredAddress) { this.blurredAddress = blurredAddress; }

    public Double getBlurredLatitude() { return blurredLatitude; }
    public void setBlurredLatitude(Double blurredLatitude) { this.blurredLatitude = blurredLatitude; }

    public Double getBlurredLongitude() { return blurredLongitude; }
    public void setBlurredLongitude(Double blurredLongitude) { this.blurredLongitude = blurredLongitude; }
}

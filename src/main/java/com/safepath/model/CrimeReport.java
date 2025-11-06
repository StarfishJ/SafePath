package com.safepath.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "crime_reports")
public class CrimeReport {

    @Id
    @Column(name = "report_number", length = 50)
    private String reportNumber;

    @Column(name = "report_datetime")
    private LocalDateTime reportDatetime;

    @Column(name = "precinct", length = 100)
    private String precinct;

    @Column(name = "sector", length = 10)
    private String sector;

    @Column(name = "beat", length = 10)
    private String beat;

    @Column(name = "mcpp_neighborhood", length = 100)
    private String mcppNeighborhood;

    @Column(name = "blurred_address", length = 255)
    private String blurredAddress;

    @Column(name = "blurred_latitude")
    private Double blurredLatitude;

    @Column(name = "blurred_longitude")
    private Double blurredLongitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etl_id")
    private EtlRun etlRun;

    public CrimeReport() {
    }

    public String getReportNumber() {
        return reportNumber;
    }

    public void setReportNumber(String reportNumber) {
        this.reportNumber = reportNumber;
    }

    public LocalDateTime getReportDatetime() {
        return reportDatetime;
    }

    public void setReportDatetime(LocalDateTime reportDatetime) {
        this.reportDatetime = reportDatetime;
    }

    public String getPrecinct() {
        return precinct;
    }

    public void setPrecinct(String precinct) {
        this.precinct = precinct;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getBeat() {
        return beat;
    }

    public void setBeat(String beat) {
        this.beat = beat;
    }

    public String getMcppNeighborhood() {
        return mcppNeighborhood;
    }

    public void setMcppNeighborhood(String mcppNeighborhood) {
        this.mcppNeighborhood = mcppNeighborhood;
    }

    public String getBlurredAddress() {
        return blurredAddress;
    }

    public void setBlurredAddress(String blurredAddress) {
        this.blurredAddress = blurredAddress;
    }

    public Double getBlurredLatitude() {
        return blurredLatitude;
    }

    public void setBlurredLatitude(Double blurredLatitude) {
        this.blurredLatitude = blurredLatitude;
    }

    public Double getBlurredLongitude() {
        return blurredLongitude;
    }

    public void setBlurredLongitude(Double blurredLongitude) {
        this.blurredLongitude = blurredLongitude;
    }

    public EtlRun getEtlRun() {
        return etlRun;
    }

    public void setEtlRun(EtlRun etlRun) {
        this.etlRun = etlRun;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CrimeReport)) return false;
        CrimeReport that = (CrimeReport) o;
        return Objects.equals(reportNumber, that.reportNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportNumber);
    }
}

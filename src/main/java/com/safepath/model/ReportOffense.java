package com.safepath.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "report_offenses")
public class ReportOffense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "offense_id")
    private Long offenseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_number", nullable = false)
    private CrimeReport crimeReport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offense_code", nullable = false)
    private OffenseType offenseType;

    @Column(name = "offense_date", nullable = false)
    private LocalDateTime offenseDate;

    @Column(name = "offense_start_time")
    private LocalDateTime offenseStartTime;

    @Column(name = "offense_end_time")
    private LocalDateTime offenseEndTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etl_id")
    private EtlRun etlRun;

    public ReportOffense() {
    }

    public Long getOffenseId() {
        return offenseId;
    }

    public void setOffenseId(Long offenseId) {
        this.offenseId = offenseId;
    }

    public CrimeReport getCrimeReport() {
        return crimeReport;
    }

    public void setCrimeReport(CrimeReport crimeReport) {
        this.crimeReport = crimeReport;
    }

    public OffenseType getOffenseType() {
        return offenseType;
    }

    public void setOffenseType(OffenseType offenseType) {
        this.offenseType = offenseType;
    }

    public LocalDateTime getOffenseDate() {
        return offenseDate;
    }

    public void setOffenseDate(LocalDateTime offenseDate) {
        this.offenseDate = offenseDate;
    }

    public LocalDateTime getOffenseStartTime() {
        return offenseStartTime;
    }

    public void setOffenseStartTime(LocalDateTime offenseStartTime) {
        this.offenseStartTime = offenseStartTime;
    }

    public LocalDateTime getOffenseEndTime() {
        return offenseEndTime;
    }

    public void setOffenseEndTime(LocalDateTime offenseEndTime) {
        this.offenseEndTime = offenseEndTime;
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
        if (!(o instanceof ReportOffense)) return false;
        ReportOffense that = (ReportOffense) o;
        return Objects.equals(offenseId, that.offenseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offenseId);
    }
}

package com.safepath.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "etl_runs")
public class EtlRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "etl_id")
    private Integer etlId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;

    @Column(name = "run_time", nullable = false)
    private LocalDateTime runTime;

    @Column(name = "record_count")
    private Integer recordCount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public EtlRun() {
    }

    public Integer getEtlId() {
        return etlId;
    }

    public void setEtlId(Integer etlId) {
        this.etlId = etlId;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public LocalDateTime getRunTime() {
        return runTime;
    }

    public void setRunTime(LocalDateTime runTime) {
        this.runTime = runTime;
    }

    public Integer getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(Integer recordCount) {
        this.recordCount = recordCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EtlRun)) return false;
        EtlRun etlRun = (EtlRun) o;
        return Objects.equals(etlId, etlRun.etlId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(etlId);
    }
}

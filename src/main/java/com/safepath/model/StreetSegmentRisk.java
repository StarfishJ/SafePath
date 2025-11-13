package com.safepath.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "street_segment_risk")
public class StreetSegmentRisk {

    @Id
    @Column(name = "unitid", length = 50)
    private String unitid;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "unitid")
    private StreetSegment streetSegment;

    @Column(name = "cluster_id", nullable = false)
    private Integer clusterId;

    @Column(name = "risk_label", length = 10, nullable = false)
    private String riskLabel;

    @Column(name = "risk_score", nullable = false)
    private Double riskScore;

    @Column(name = "incident_density")
    private Double incidentDensity;

    @Column(name = "night_fraction")
    private Double nightFraction;

    @Column(name = "last_90d_incidents")
    private Integer last90dIncidents;

    @Column(name = "model_version", length = 20)
    private String modelVersion;

    @Column(name = "override_reason", length = 255)
    private String overrideReason;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public StreetSegmentRisk() {
    }

    public String getUnitid() {
        return unitid;
    }

    public void setUnitid(String unitid) {
        this.unitid = unitid;
    }

    public StreetSegment getStreetSegment() {
        return streetSegment;
    }

    public void setStreetSegment(StreetSegment streetSegment) {
        this.streetSegment = streetSegment;
    }

    public Integer getClusterId() {
        return clusterId;
    }

    public void setClusterId(Integer clusterId) {
        this.clusterId = clusterId;
    }

    public String getRiskLabel() {
        return riskLabel;
    }

    public void setRiskLabel(String riskLabel) {
        this.riskLabel = riskLabel;
    }

    public Double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }

    public Double getIncidentDensity() {
        return incidentDensity;
    }

    public void setIncidentDensity(Double incidentDensity) {
        this.incidentDensity = incidentDensity;
    }

    public Double getNightFraction() {
        return nightFraction;
    }

    public void setNightFraction(Double nightFraction) {
        this.nightFraction = nightFraction;
    }

    public Integer getLast90dIncidents() {
        return last90dIncidents;
    }

    public void setLast90dIncidents(Integer last90dIncidents) {
        this.last90dIncidents = last90dIncidents;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getOverrideReason() {
        return overrideReason;
    }

    public void setOverrideReason(String overrideReason) {
        this.overrideReason = overrideReason;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StreetSegmentRisk)) return false;
        StreetSegmentRisk that = (StreetSegmentRisk) o;
        return Objects.equals(unitid, that.unitid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unitid);
    }
}


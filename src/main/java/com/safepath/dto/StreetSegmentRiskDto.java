package com.safepath.dto;

import java.time.LocalDateTime;

public class StreetSegmentRiskDto {

    private String unitid;
    private String streetName;
    private String riskLabel;
    private Double riskScore;
    private Integer clusterId;
    private Double incidentDensity;
    private Double nightFraction;
    private Integer last90dIncidents;
    private String modelVersion;
    private String overrideReason;
    private LocalDateTime updatedAt;
    private Double latitude;
    private Double longitude;

    public String getUnitid() {
        return unitid;
    }

    public void setUnitid(String unitid) {
        this.unitid = unitid;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
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

    public Integer getClusterId() {
        return clusterId;
    }

    public void setClusterId(Integer clusterId) {
        this.clusterId = clusterId;
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

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}


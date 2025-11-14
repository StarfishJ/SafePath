package com.safepath.dto;

import jakarta.validation.constraints.NotNull;

public class CreateUserAlertRequest {

    @NotNull
    private Integer userId;

    private Integer radiusM;

    private Double centerLat;

    private Double centerLon;

    private String crimeTypeFilter;

    private Boolean activeFlag = Boolean.TRUE;

    public CreateUserAlertRequest() {
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getRadiusM() {
        return radiusM;
    }

    public void setRadiusM(Integer radiusM) {
        this.radiusM = radiusM;
    }

    public Double getCenterLat() {
        return centerLat;
    }

    public void setCenterLat(Double centerLat) {
        this.centerLat = centerLat;
    }

    public Double getCenterLon() {
        return centerLon;
    }

    public void setCenterLon(Double centerLon) {
        this.centerLon = centerLon;
    }

    public String getCrimeTypeFilter() {
        return crimeTypeFilter;
    }

    public void setCrimeTypeFilter(String crimeTypeFilter) {
        this.crimeTypeFilter = crimeTypeFilter;
    }

    public Boolean getActiveFlag() {
        return activeFlag;
    }

    public void setActiveFlag(Boolean activeFlag) {
        this.activeFlag = activeFlag;
    }
}

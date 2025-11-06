package com.safepath.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "realtime_incidents")
public class RealtimeIncident {

    @Id
    @Column(name = "incident_id", length = 50)
    private String incidentId;

    @Column(name = "incident_type", length = 100)
    private String incidentType;

    @Column(name = "event_datetime")
    private LocalDateTime eventDatetime;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "report_location", length = 255)
    private String reportLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private Source source;

    public RealtimeIncident() {
    }

    public String getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(String incidentId) {
        this.incidentId = incidentId;
    }

    public String getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(String incidentType) {
        this.incidentType = incidentType;
    }

    public LocalDateTime getEventDatetime() {
        return eventDatetime;
    }

    public void setEventDatetime(LocalDateTime eventDatetime) {
        this.eventDatetime = eventDatetime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getReportLocation() {
        return reportLocation;
    }

    public void setReportLocation(String reportLocation) {
        this.reportLocation = reportLocation;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RealtimeIncident)) return false;
        RealtimeIncident that = (RealtimeIncident) o;
        return Objects.equals(incidentId, that.incidentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(incidentId);
    }
}

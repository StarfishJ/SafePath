package com.safepath.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "street_segments")
public class StreetSegment {

    @Id
    @Column(name = "unitid", length = 50)
    private String unitid;

    @Column(name = "onstreet", length = 255)
    private String onstreet;

    @Column(name = "speedlimit")
    private Integer speedlimit;

    @Column(name = "artclass")
    private Integer artclass;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "seglength")
    private Integer seglength;

    @Column(name = "surfacewidth")
    private Integer surfacewidth;

    @Column(name = "slope_pct")
    private Double slopePct;

    @Column(name = "owner", length = 100)
    private String owner;

    @Column(name = "oneway", length = 1)
    private String oneway;

    @Column(name = "flow", length = 50)
    private String flow;

    @Column(name = "gis_mid_x")
    private Double gisMidX;

    @Column(name = "gis_mid_y")
    private Double gisMidY;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "start_intkey")
    private Intersection startIntersection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "end_intkey")
    private Intersection endIntersection;

    public StreetSegment() {
    }

    public String getUnitid() {
        return unitid;
    }

    public void setUnitid(String unitid) {
        this.unitid = unitid;
    }

    public String getOnstreet() {
        return onstreet;
    }

    public void setOnstreet(String onstreet) {
        this.onstreet = onstreet;
    }

    public Integer getSpeedlimit() {
        return speedlimit;
    }

    public void setSpeedlimit(Integer speedlimit) {
        this.speedlimit = speedlimit;
    }

    public Integer getArtclass() {
        return artclass;
    }

    public void setArtclass(Integer artclass) {
        this.artclass = artclass;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getSeglength() {
        return seglength;
    }

    public void setSeglength(Integer seglength) {
        this.seglength = seglength;
    }

    public Integer getSurfacewidth() {
        return surfacewidth;
    }

    public void setSurfacewidth(Integer surfacewidth) {
        this.surfacewidth = surfacewidth;
    }

    public Double getSlopePct() {
        return slopePct;
    }

    public void setSlopePct(Double slopePct) {
        this.slopePct = slopePct;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOneway() {
        return oneway;
    }

    public void setOneway(String oneway) {
        this.oneway = oneway;
    }

    public String getFlow() {
        return flow;
    }

    public void setFlow(String flow) {
        this.flow = flow;
    }

    public Double getGisMidX() {
        return gisMidX;
    }

    public void setGisMidX(Double gisMidX) {
        this.gisMidX = gisMidX;
    }

    public Double getGisMidY() {
        return gisMidY;
    }

    public void setGisMidY(Double gisMidY) {
        this.gisMidY = gisMidY;
    }

    public Intersection getStartIntersection() {
        return startIntersection;
    }

    public void setStartIntersection(Intersection startIntersection) {
        this.startIntersection = startIntersection;
    }

    public Intersection getEndIntersection() {
        return endIntersection;
    }

    public void setEndIntersection(Intersection endIntersection) {
        this.endIntersection = endIntersection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StreetSegment)) return false;
        StreetSegment that = (StreetSegment) o;
        return Objects.equals(unitid, that.unitid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unitid);
    }
}

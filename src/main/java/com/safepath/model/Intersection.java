package com.safepath.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "intersections")
public class Intersection {

    @Id
    @Column(name = "intkey", length = 50)
    private String intkey;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "direction", length = 10)
    private String direction;

    @Column(name = "gis_x")
    private Double gisX;

    @Column(name = "gis_y")
    private Double gisY;

    public Intersection() {
    }

    public String getIntkey() {
        return intkey;
    }

    public void setIntkey(String intkey) {
        this.intkey = intkey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Double getGisX() {
        return gisX;
    }

    public void setGisX(Double gisX) {
        this.gisX = gisX;
    }

    public Double getGisY() {
        return gisY;
    }

    public void setGisY(Double gisY) {
        this.gisY = gisY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Intersection)) return false;
        Intersection that = (Intersection) o;
        return Objects.equals(intkey, that.intkey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(intkey);
    }
}

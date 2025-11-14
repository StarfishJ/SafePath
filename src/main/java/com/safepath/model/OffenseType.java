package com.safepath.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "offense_types")
public class OffenseType {

    @Id
    @Column(name = "offense_code", length = 10)
    private String offenseCode;

    @Column(name = "offense_name", length = 100, nullable = false)
    private String offenseName;

    @Column(name = "offense_parent_group", length = 100)
    private String offenseParentGroup;

    @Column(name = "group_a_b", length = 1)
    private String groupAB;

    @Column(name = "crime_against_category", length = 50)
    private String crimeAgainstCategory;

    public OffenseType() {
    }

    public String getOffenseCode() {
        return offenseCode;
    }

    public void setOffenseCode(String offenseCode) {
        this.offenseCode = offenseCode;
    }

    public String getOffenseName() {
        return offenseName;
    }

    public void setOffenseName(String offenseName) {
        this.offenseName = offenseName;
    }

    public String getOffenseParentGroup() {
        return offenseParentGroup;
    }

    public void setOffenseParentGroup(String offenseParentGroup) {
        this.offenseParentGroup = offenseParentGroup;
    }

    public String getGroupAB() {
        return groupAB;
    }

    public void setGroupAB(String groupAB) {
        this.groupAB = groupAB;
    }

    public String getCrimeAgainstCategory() {
        return crimeAgainstCategory;
    }

    public void setCrimeAgainstCategory(String crimeAgainstCategory) {
        this.crimeAgainstCategory = crimeAgainstCategory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OffenseType)) return false;
        OffenseType that = (OffenseType) o;
        return Objects.equals(offenseCode, that.offenseCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offenseCode);
    }
}

package com.tneaguider.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "colleges")
public class College {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String branch;

    private String category;

    @ManyToOne
    @JoinColumn(name = "district_id")
    private District district;

    private Double cutoff;

    private Double fees;

    private Double placementRate;

    private String tier;

    public College() {
    }

    public College(Long id, String name, String branch, String category, District district, Double cutoff, Double fees,
                   Double placementRate, String tier) {
        this.id = id;
        this.name = name;
        this.branch = branch;
        this.category = category;
        this.district = district;
        this.cutoff = cutoff;
        this.fees = fees;
        this.placementRate = placementRate;
        this.tier = tier;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public District getDistrict() {
        return district;
    }

    public void setDistrict(District district) {
        this.district = district;
    }

    public String getDistrictName() {
        return district != null && district.getName() != null ? district.getName() : "Unknown";
    }

    public Double getCutoff() {
        return cutoff;
    }

    public void setCutoff(Double cutoff) {
        this.cutoff = cutoff;
    }

    public Double getFees() {
        return fees;
    }

    public void setFees(Double fees) {
        this.fees = fees;
    }

    public Double getPlacementRate() {
        return placementRate;
    }

    public void setPlacementRate(Double placementRate) {
        this.placementRate = placementRate;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }
}
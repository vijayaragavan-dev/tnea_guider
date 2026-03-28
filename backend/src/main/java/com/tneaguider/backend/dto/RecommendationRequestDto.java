package com.tneaguider.backend.dto;

import com.tneaguider.backend.validation.HalfStep;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class RecommendationRequestDto {

    @NotNull(message = "cutoff is required")
    @DecimalMin(value = "0.0", message = "cutoff must be at least 0")
    @DecimalMax(value = "200.0", message = "cutoff must be at most 200")
    @HalfStep
    private BigDecimal cutoff;

    @NotBlank(message = "category is required")
    private String category;

    private String branch;

    @DecimalMin(value = "50000", message = "budget must be at least 50000")
    private BigDecimal budget;

    private String district;

    private String preference;

    public RecommendationRequestDto() {
    }

    public RecommendationRequestDto(BigDecimal cutoff, String category, String branch, BigDecimal budget, String district) {
        this.cutoff = cutoff;
        this.category = category;
        this.branch = branch;
        this.budget = budget;
        this.district = district;
    }

    public BigDecimal getCutoff() {
        return cutoff;
    }

    public void setCutoff(BigDecimal cutoff) {
        this.cutoff = cutoff;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getPreference() {
        return preference;
    }

    public void setPreference(String preference) {
        this.preference = preference;
    }
}

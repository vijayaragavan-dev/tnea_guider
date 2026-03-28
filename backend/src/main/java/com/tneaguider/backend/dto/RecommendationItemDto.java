package com.tneaguider.backend.dto;

public class RecommendationItemDto {

    private Long collegeId;
    private String collegeName;
    private String district;
    private Double cutoff;
    private Double fees;
    private Double placementRate;
    private String tier;
    private Double finalScore;

    public RecommendationItemDto() {
    }

    public RecommendationItemDto(Long collegeId, String collegeName, String district, Double cutoff, Double fees,
                                 Double placementRate, String tier, Double finalScore) {
        this.collegeId = collegeId;
        this.collegeName = collegeName;
        this.district = district;
        this.cutoff = cutoff;
        this.fees = fees;
        this.placementRate = placementRate;
        this.tier = tier;
        this.finalScore = finalScore;
    }

    public Long getCollegeId() {
        return collegeId;
    }

    public void setCollegeId(Long collegeId) {
        this.collegeId = collegeId;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
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

    public Double getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(Double finalScore) {
        this.finalScore = finalScore;
    }
}

package com.tneaguider.backend.dto;

import java.util.ArrayList;
import java.util.List;

public class RecommendationResponseDto {

    private List<RecommendationItemDto> dream = new ArrayList<>();
    private List<RecommendationItemDto> moderate = new ArrayList<>();
    private List<RecommendationItemDto> safe = new ArrayList<>();

    public RecommendationResponseDto() {
    }

    public RecommendationResponseDto(List<RecommendationItemDto> dream, List<RecommendationItemDto> moderate,
                                     List<RecommendationItemDto> safe) {
        this.dream = dream;
        this.moderate = moderate;
        this.safe = safe;
    }

    public List<RecommendationItemDto> getDream() {
        return dream;
    }

    public void setDream(List<RecommendationItemDto> dream) {
        this.dream = dream;
    }

    public List<RecommendationItemDto> getModerate() {
        return moderate;
    }

    public void setModerate(List<RecommendationItemDto> moderate) {
        this.moderate = moderate;
    }

    public List<RecommendationItemDto> getSafe() {
        return safe;
    }

    public void setSafe(List<RecommendationItemDto> safe) {
        this.safe = safe;
    }
}

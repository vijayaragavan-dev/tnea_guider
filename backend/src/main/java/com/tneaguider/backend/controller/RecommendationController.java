package com.tneaguider.backend.controller;

import com.tneaguider.backend.dto.RecommendationRequestDto;
import com.tneaguider.backend.dto.RecommendationResponseDto;
import com.tneaguider.backend.dto.RecommendationItemDto;
import com.tneaguider.backend.service.RecommendationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping("/recommend")
    public ResponseEntity<RecommendationResponseDto> recommend(@Valid @RequestBody RecommendationRequestDto request) {
        RecommendationResponseDto response = recommendationService.generateRecommendations(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/compare")
    public ResponseEntity<List<RecommendationItemDto>> compare(@RequestParam("ids") String ids) {
        List<Long> collegeIds = parseCollegeIds(ids);

        List<RecommendationItemDto> response = recommendationService.compareColleges(collegeIds);
        return ResponseEntity.ok(response);
    }

    private List<Long> parseCollegeIds(String ids) {
        if (ids == null || ids.isBlank()) {
            throw new IllegalArgumentException("ids query parameter is required.");
        }

        List<String> tokens = Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .toList();

        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("ids query parameter is required.");
        }

        if (tokens.size() > 3) {
            throw new IllegalArgumentException("You can compare maximum 3 colleges.");
        }

        try {
            List<Long> parsedIds = tokens.stream()
                    .map(Long::valueOf)
                    .toList();

            boolean hasNonPositive = parsedIds.stream().anyMatch(id -> id <= 0);
            if (hasNonPositive) {
                throw new IllegalArgumentException("ids must contain only positive numbers.");
            }

            return parsedIds;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("ids must be comma-separated numeric values.");
        }
    }
}

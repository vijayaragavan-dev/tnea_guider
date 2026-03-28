package com.tneaguider.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tneaguider.backend.dto.RecommendationItemDto;
import com.tneaguider.backend.dto.RecommendationRequestDto;
import com.tneaguider.backend.dto.RecommendationResponseDto;
import com.tneaguider.backend.service.RecommendationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecommendationController.class)
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RecommendationService recommendationService;

    @Test
    void shouldReturnRecommendationsForValidPayload() throws Exception {
        RecommendationResponseDto response = new RecommendationResponseDto();
        response.setDream(List.of(new RecommendationItemDto(1L, "College A", "Chennai", 188.0, 90000.0, 85.0, "Tier 1", 0.91)));
        response.setModerate(List.of());
        response.setSafe(List.of());

        when(recommendationService.generateRecommendations(any(RecommendationRequestDto.class))).thenReturn(response);

        RecommendationRequestDto request = new RecommendationRequestDto(
                new BigDecimal("187.5"),
                "BC",
                "CSE",
                new BigDecimal("100000"),
                "Chennai"
        );

        mockMvc.perform(post("/api/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dream[0].collegeName").value("College A"));
    }

    @Test
    void shouldRejectInvalidCutoffDecimal() throws Exception {
        String invalidPayload = """
                {
                  "cutoff": 187.7,
                  "category": "BC",
                  "branch": "CSE",
                  "budget": 100000,
                  "district": "Chennai"
                }
                """;

        mockMvc.perform(post("/api/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldAcceptPayloadWithOnlyMandatoryFields() throws Exception {
        RecommendationResponseDto response = new RecommendationResponseDto();
        response.setDream(List.of());
        response.setModerate(List.of(new RecommendationItemDto(2L, "College B", "Madurai", 185.0, 70000.0, 80.0, "Tier 2", 0.72)));
        response.setSafe(List.of());

        when(recommendationService.generateRecommendations(any(RecommendationRequestDto.class))).thenReturn(response);

        String payload = """
                {
                  "cutoff": 187.5,
                  "category": "BC"
                }
                """;

        mockMvc.perform(post("/api/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moderate[0].collegeName").value("College B"));
    }

    @Test
    void shouldReturnCollegeComparisonByIds() throws Exception {
        List<RecommendationItemDto> response = List.of(
                new RecommendationItemDto(1L, "College A", "Chennai", 188.0, 90000.0, 85.0, "Tier 1", null),
                new RecommendationItemDto(2L, "College B", "Madurai", 183.0, 70000.0, 80.0, "Tier 2", null)
        );

        when(recommendationService.compareColleges(any())).thenReturn(response);

        mockMvc.perform(get("/api/compare")
                        .param("ids", "1,2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].collegeName").value("College A"))
                .andExpect(jsonPath("$[1].collegeName").value("College B"));
    }

    @Test
    void shouldRejectCompareWhenIdsAreNonNumeric() throws Exception {
        mockMvc.perform(get("/api/compare")
                        .param("ids", "1,abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ids must be comma-separated numeric values."));

        verifyNoInteractions(recommendationService);
    }

    @Test
    void shouldRejectCompareWhenMoreThanThreeIdsProvided() throws Exception {
        mockMvc.perform(get("/api/compare")
                        .param("ids", "1,2,3,4"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You can compare maximum 3 colleges."));

        verifyNoInteractions(recommendationService);
    }
}

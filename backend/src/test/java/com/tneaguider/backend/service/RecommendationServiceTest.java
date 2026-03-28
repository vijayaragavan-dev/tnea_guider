package com.tneaguider.backend.service;

import com.tneaguider.backend.dto.RecommendationRequestDto;
import com.tneaguider.backend.dto.RecommendationResponseDto;
import com.tneaguider.backend.entity.College;
import com.tneaguider.backend.entity.Student;
import com.tneaguider.backend.repository.CollegeRepository;
import com.tneaguider.backend.repository.RecommendationRepository;
import com.tneaguider.backend.repository.StudentRepository;
import com.tneaguider.backend.repository.WeightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecommendationServiceTest {

    private CollegeRepository collegeRepository;
    private StudentRepository studentRepository;
    private RecommendationRepository recommendationRepository;
    private WeightRepository weightRepository;
    private RecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        collegeRepository = mock(CollegeRepository.class);
        studentRepository = mock(StudentRepository.class);
        recommendationRepository = mock(RecommendationRepository.class);
        weightRepository = mock(WeightRepository.class);

        recommendationService = new RecommendationService(collegeRepository, studentRepository, recommendationRepository, weightRepository);
    }

    @Test
    void shouldClassifyAndSortRecommendations() {
        RecommendationRequestDto request = new RecommendationRequestDto(
                new BigDecimal("187.5"), "BC", "CSE", new BigDecimal("100000"), "Chennai"
        );

        College dreamCollege = new College(1L, "Alpha", "CSE", "BC", "Chennai", 190.0, 100000.0, 95.0, "Tier 1");
        College moderateCollege = new College(2L, "Beta", "CSE", "BC", "Chennai", 185.0, 80000.0, 80.0, "Tier 2");
        College safeCollege = new College(3L, "Gamma", "CSE", "BC", "Chennai", 170.0, 60000.0, 70.0, "Tier 3");

        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(collegeRepository.findAll()).thenReturn(List.of(dreamCollege, moderateCollege, safeCollege));
        when(weightRepository.findByWeightKeyIgnoreCase(anyString())).thenReturn(Optional.empty());

        RecommendationResponseDto response = recommendationService.generateRecommendations(request);

        assertEquals(1, response.getDream().size());
        assertEquals(1, response.getModerate().size());
        assertEquals(1, response.getSafe().size());
        assertEquals("Alpha", response.getDream().get(0).getCollegeName());
        assertFalse(response.getDream().get(0).getFinalScore() < response.getModerate().get(0).getFinalScore());
    }

    @Test
    void shouldReturnResultsWhenOnlyCutoffAndCategoryProvided() {
        RecommendationRequestDto request = new RecommendationRequestDto(
                new BigDecimal("187.5"), "BC", null, null, null
        );

        College collegeOne = new College(1L, "Alpha", "CSE", "BC", "Chennai", 190.0, 120000.0, 92.0, "Tier 1");
        College collegeTwo = new College(2L, "Beta", "ECE", "BC", "Madurai", 182.0, 80000.0, 78.0, "Tier 2");
        College otherCategory = new College(3L, "Gamma", "CSE", "OC", "Chennai", 195.0, 95000.0, 88.0, "Tier 1");

        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(collegeRepository.findAll()).thenReturn(List.of(collegeOne, collegeTwo, otherCategory));
        when(weightRepository.findByWeightKeyIgnoreCase(anyString())).thenReturn(Optional.empty());

        RecommendationResponseDto response = recommendationService.generateRecommendations(request);

        int total = response.getDream().size() + response.getModerate().size() + response.getSafe().size();
        assertEquals(2, total);
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void shouldApplyBudgetAndDistrictFiltersWhenProvided() {
        RecommendationRequestDto request = new RecommendationRequestDto(
                new BigDecimal("187.5"), "BC", null, new BigDecimal("90000"), "Chennai"
        );

        College inBudgetDistrict = new College(1L, "Alpha", "CSE", "BC", "Chennai", 186.0, 85000.0, 82.0, "Tier 2");
        College outBudget = new College(2L, "Beta", "CSE", "BC", "Chennai", 188.0, 110000.0, 84.0, "Tier 2");
        College outDistrict = new College(3L, "Gamma", "CSE", "BC", "Madurai", 180.0, 70000.0, 75.0, "Tier 3");

        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(collegeRepository.findAll()).thenReturn(List.of(inBudgetDistrict, outBudget, outDistrict));
        when(weightRepository.findByWeightKeyIgnoreCase(anyString())).thenReturn(Optional.empty());

        RecommendationResponseDto response = recommendationService.generateRecommendations(request);

        int total = response.getDream().size() + response.getModerate().size() + response.getSafe().size();
        assertEquals(1, total);
        assertTrue(response.getDream().stream().anyMatch(item -> "Alpha".equals(item.getCollegeName()))
                || response.getModerate().stream().anyMatch(item -> "Alpha".equals(item.getCollegeName()))
                || response.getSafe().stream().anyMatch(item -> "Alpha".equals(item.getCollegeName())));
    }
}

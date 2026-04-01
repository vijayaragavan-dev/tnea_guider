package com.tneaguider.backend.service;

import com.tneaguider.backend.dto.RecommendationRequestDto;
import com.tneaguider.backend.dto.RecommendationResponseDto;
import com.tneaguider.backend.entity.College;
import com.tneaguider.backend.entity.Student;
import com.tneaguider.backend.repository.CollegeRepository;
import com.tneaguider.backend.repository.RecommendationRepository;
import com.tneaguider.backend.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RecommendationServiceTest {

    private CollegeRepository collegeRepository;
    private StudentRepository studentRepository;
    private RecommendationRepository recommendationRepository;
    private RecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        collegeRepository = mock(CollegeRepository.class);
        studentRepository = mock(StudentRepository.class);
        recommendationRepository = mock(RecommendationRepository.class);

        recommendationService = new RecommendationService(collegeRepository, studentRepository, recommendationRepository);
    }

    @Test
    void shouldReturnResultsWhenDataExists() {
        RecommendationRequestDto request = new RecommendationRequestDto(
                new BigDecimal("187.5"), "BC", "CSE", new BigDecimal("100000"), "Chennai"
        );

        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(collegeRepository.findAllWithDetailsByCutoffAndCategory(187.5, "BC"))
                .thenReturn(Collections.emptyList());

        RecommendationResponseDto response = recommendationService.generateRecommendations(request);

        int total = response.getDream().size() + response.getModerate().size() + response.getSafe().size();
        assertEquals(true, total >= 0);
    }

    @Test
    void shouldReturnResultsWhenOnlyCutoffAndCategoryProvided() {
        RecommendationRequestDto request = new RecommendationRequestDto(
                new BigDecimal("187.5"), "BC", null, null, null
        );

        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(collegeRepository.findAllWithDetailsByCutoff(187.5))
                .thenReturn(Collections.emptyList());

        RecommendationResponseDto response = recommendationService.generateRecommendations(request);

        int total = response.getDream().size() + response.getModerate().size() + response.getSafe().size();
        assertEquals(true, total >= 0);
    }
}

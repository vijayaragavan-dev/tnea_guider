package com.tneaguider.backend.service;

import com.tneaguider.backend.dto.RecommendationItemDto;
import com.tneaguider.backend.dto.RecommendationRequestDto;
import com.tneaguider.backend.dto.RecommendationResponseDto;
import com.tneaguider.backend.entity.College;
import com.tneaguider.backend.entity.Recommendation;
import com.tneaguider.backend.entity.Student;
import com.tneaguider.backend.entity.Weight;
import com.tneaguider.backend.exception.NoResultsFoundException;
import com.tneaguider.backend.repository.CollegeRepository;
import com.tneaguider.backend.repository.RecommendationRepository;
import com.tneaguider.backend.repository.StudentRepository;
import com.tneaguider.backend.repository.WeightRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final double CUTOFF_WEIGHT = 0.4;
    private static final double PLACEMENT_WEIGHT = 0.2;
    private static final double TIER_WEIGHT = 0.2;
    private static final double FEE_WEIGHT = 0.2;

    private final CollegeRepository collegeRepository;
    private final StudentRepository studentRepository;
    private final RecommendationRepository recommendationRepository;
    private final WeightRepository weightRepository;

    public RecommendationService(CollegeRepository collegeRepository,
                                 StudentRepository studentRepository,
                                 RecommendationRepository recommendationRepository,
                                 WeightRepository weightRepository) {
        this.collegeRepository = collegeRepository;
        this.studentRepository = studentRepository;
        this.recommendationRepository = recommendationRepository;
        this.weightRepository = weightRepository;
    }

    @Transactional
    public RecommendationResponseDto generateRecommendations(RecommendationRequestDto request) {
        double studentCutoff = request.getCutoff().doubleValue();
        Double budget = request.getBudget() == null ? null : request.getBudget().doubleValue();
        WeightProfile weightProfile = resolveWeightProfile(request.getPreference());

        Student savedStudent = saveStudentSnapshot(request, studentCutoff, budget);

        List<College> filteredColleges = filterColleges(
                collegeRepository.findAll(),
                request.getBranch(),
                request.getCategory(),
                budget,
                request.getDistrict()
        );

        if (filteredColleges.isEmpty()) {
            throw new NoResultsFoundException("No colleges found for the selected criteria.");
        }

        double maxFees = filteredColleges.stream()
                .map(College::getFees)
                .filter(Objects::nonNull)
                .max(Double::compareTo)
                .orElse(1.0);

        RecommendationResponseDto response = new RecommendationResponseDto();
        List<Recommendation> recordsToPersist = new ArrayList<>();

        for (College college : filteredColleges) {
            double score = calculateFinalScore(studentCutoff, college, maxFees, weightProfile);
            String classification = classify(studentCutoff, college.getCutoff());
            RecommendationItemDto item = mapToItemDto(college, score);

            addToBucket(response, classification, item);
            recordsToPersist.add(buildRecommendationEntity(savedStudent, college, score, classification));
        }

        sortBuckets(response);
        recommendationRepository.saveAll(recordsToPersist);
        ensureHasAtLeastOneResult(response);

        return response;
    }

    public List<RecommendationItemDto> compareColleges(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("At least one college id is required for comparison.");
        }

        LinkedHashSet<Long> uniqueValidIds = new LinkedHashSet<>();
        for (Long id : ids) {
            if (id == null) {
                continue;
            }
            if (id <= 0) {
                throw new IllegalArgumentException("College ids must be positive numbers.");
            }
            uniqueValidIds.add(id);
        }

        if (uniqueValidIds.isEmpty()) {
            throw new IllegalArgumentException("At least one college id is required for comparison.");
        }

        if (uniqueValidIds.size() > 3) {
            throw new IllegalArgumentException("You can compare maximum 3 colleges.");
        }

        List<Long> normalizedIds = new ArrayList<>(uniqueValidIds);

        List<College> colleges = collegeRepository.findAllById(normalizedIds);
        if (colleges.isEmpty()) {
            throw new NoResultsFoundException("No colleges found for the provided ids.");
        }

        Map<Long, College> byId = new HashMap<>();
        for (College college : colleges) {
            byId.put(college.getId(), college);
        }

        List<RecommendationItemDto> response = new ArrayList<>();
        for (Long id : normalizedIds) {
            College college = byId.get(id);
            if (college == null) {
                continue;
            }

            RecommendationItemDto dto = new RecommendationItemDto();
            dto.setCollegeId(college.getId());
            dto.setCollegeName(college.getName());
            dto.setDistrict(college.getDistrict());
            dto.setCutoff(college.getCutoff());
            dto.setFees(college.getFees());
            dto.setPlacementRate(college.getPlacementRate());
            dto.setTier(college.getTier());
            dto.setFinalScore(null);
            response.add(dto);
        }

        if (response.isEmpty()) {
            throw new NoResultsFoundException("No colleges found for the provided ids.");
        }

        return response;
    }

    private Student saveStudentSnapshot(RecommendationRequestDto request, double studentCutoff, Double budget) {
        Student student = new Student();
        student.setCutoff(studentCutoff);
        student.setCategory(request.getCategory().trim().toUpperCase(Locale.ROOT));
        String normalizedBranch = normalizeOptionalText(request.getBranch());
        student.setBranch(normalizedBranch == null ? "ANY" : normalizedBranch.toUpperCase(Locale.ROOT));
        student.setBudget(budget == null ? 0.0 : budget);
        student.setDistrict(normalizeDistrict(request.getDistrict()));
        student.setCreatedAt(LocalDateTime.now());
        return studentRepository.save(student);
    }

    private List<College> filterColleges(List<College> colleges,
                                         String branch,
                                         String category,
                                         Double budget,
                                         String district) {
        String normalizedBranch = normalizeOptionalText(branch);
        String normalizedCategory = category.trim();
        String normalizedDistrict = normalizeOptionalText(district);
        boolean hasBudgetFilter = budget != null && budget > 0;

        return colleges.stream()
                .filter(c -> c.getCategory() != null && c.getCategory().equalsIgnoreCase(normalizedCategory))
                .filter(c -> normalizedBranch == null
                        || (c.getBranch() != null && c.getBranch().equalsIgnoreCase(normalizedBranch)))
                .filter(c -> !hasBudgetFilter
                        || (c.getFees() != null && c.getFees() <= budget))
                .filter(c -> normalizedDistrict == null
                        || (c.getDistrict() != null && normalizedDistrict.equalsIgnoreCase(c.getDistrict().trim())))
                .collect(Collectors.toList());
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeDistrict(String district) {
        String normalized = normalizeOptionalText(district);
        return normalized == null ? null : normalized;
    }

    private double calculateFinalScore(double studentCutoff, College college, double maxFees, WeightProfile profile) {
        double collegeCutoff = Optional.ofNullable(college.getCutoff()).orElse(0.0);
        double placementRate = Optional.ofNullable(college.getPlacementRate()).orElse(0.0);
        double tierWeight = resolveTierWeight(college.getTier());
        double fees = Optional.ofNullable(college.getFees()).orElse(maxFees);

        double cutoffScore = collegeCutoff <= 0 ? 0 : studentCutoff / collegeCutoff;
        double feeScore = maxFees <= 0 ? 0 : (1 - (fees / maxFees));

        return (profile.cutoffWeight * cutoffScore)
                + (profile.placementWeight * (placementRate / 100.0))
                + (profile.tierWeight * tierWeight)
                + (profile.feeWeight * feeScore);
    }

    private WeightProfile resolveWeightProfile(String preference) {
        if (preference == null || preference.isBlank()) {
            return new WeightProfile(CUTOFF_WEIGHT, PLACEMENT_WEIGHT, TIER_WEIGHT, FEE_WEIGHT);
        }

        String normalized = preference.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "high placement", "placement" -> normalizeWeights(0.3, 0.4, 0.2, 0.1);
            case "low fees", "fees" -> normalizeWeights(0.3, 0.15, 0.15, 0.4);
            case "top tier", "tier" -> normalizeWeights(0.3, 0.15, 0.4, 0.15);
            default -> new WeightProfile(CUTOFF_WEIGHT, PLACEMENT_WEIGHT, TIER_WEIGHT, FEE_WEIGHT);
        };
    }

    private WeightProfile normalizeWeights(double cutoffWeight,
                                           double placementWeight,
                                           double tierWeight,
                                           double feeWeight) {
        List<Double> raw = Arrays.asList(cutoffWeight, placementWeight, tierWeight, feeWeight);
        double safeSum = raw.stream().mapToDouble(v -> Math.max(0.0, v)).sum();
        if (safeSum <= 0) {
            return new WeightProfile(CUTOFF_WEIGHT, PLACEMENT_WEIGHT, TIER_WEIGHT, FEE_WEIGHT);
        }

        return new WeightProfile(
                Math.max(0.0, cutoffWeight) / safeSum,
                Math.max(0.0, placementWeight) / safeSum,
                Math.max(0.0, tierWeight) / safeSum,
                Math.max(0.0, feeWeight) / safeSum
        );
    }

    private double resolveTierWeight(String tier) {
        if (tier == null || tier.isBlank()) {
            return 0.5;
        }

        Map<String, Double> defaultWeights = Map.of(
                "TIER 1", 1.0,
                "TIER 2", 0.7,
                "TIER 3", 0.5
        );

        String normalizedKey = tier.trim().toUpperCase(Locale.ROOT);
        Optional<Weight> configuredWeight = weightRepository.findByWeightKeyIgnoreCase(normalizedKey);
        if (configuredWeight.isPresent()) {
            return configuredWeight.get().getWeightValue();
        }

        return defaultWeights.getOrDefault(normalizedKey, 0.5);
    }

    private String classify(double studentCutoff, Double collegeCutoffValue) {
        double collegeCutoff = Optional.ofNullable(collegeCutoffValue).orElse(0.0);

        if (collegeCutoff >= studentCutoff) {
            return "DREAM";
        }
        if (Math.abs(studentCutoff - collegeCutoff) <= 5.0) {
            return "MODERATE";
        }
        return "SAFE";
    }

    private RecommendationItemDto mapToItemDto(College college, double score) {
        RecommendationItemDto dto = new RecommendationItemDto();
        dto.setCollegeId(college.getId());
        dto.setCollegeName(college.getName());
        dto.setDistrict(college.getDistrict());
        dto.setCutoff(college.getCutoff());
        dto.setFees(college.getFees());
        dto.setPlacementRate(college.getPlacementRate());
        dto.setTier(college.getTier());
        dto.setFinalScore(round(score));
        return dto;
    }

    private Recommendation buildRecommendationEntity(Student student, College college, double score, String classification) {
        Recommendation recommendation = new Recommendation();
        recommendation.setStudent(student);
        recommendation.setCollege(college);
        recommendation.setFinalScore(round(score));
        recommendation.setClassification(classification);
        recommendation.setCreatedAt(LocalDateTime.now());
        return recommendation;
    }

    private void addToBucket(RecommendationResponseDto response, String classification, RecommendationItemDto item) {
        switch (classification) {
            case "DREAM" -> response.getDream().add(item);
            case "MODERATE" -> response.getModerate().add(item);
            default -> response.getSafe().add(item);
        }
    }

    private void sortBuckets(RecommendationResponseDto response) {
        Comparator<RecommendationItemDto> byScoreDesc = Comparator.comparing(RecommendationItemDto::getFinalScore).reversed();
        response.getDream().sort(byScoreDesc);
        response.getModerate().sort(byScoreDesc);
        response.getSafe().sort(byScoreDesc);
    }

    private void ensureHasAtLeastOneResult(RecommendationResponseDto response) {
        if (response.getDream().isEmpty() && response.getModerate().isEmpty() && response.getSafe().isEmpty()) {
            throw new NoResultsFoundException("No recommendations available for the selected inputs.");
        }
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    private static class WeightProfile {
        private final double cutoffWeight;
        private final double placementWeight;
        private final double tierWeight;
        private final double feeWeight;

        private WeightProfile(double cutoffWeight, double placementWeight, double tierWeight, double feeWeight) {
            this.cutoffWeight = cutoffWeight;
            this.placementWeight = placementWeight;
            this.tierWeight = tierWeight;
            this.feeWeight = feeWeight;
        }
    }
}

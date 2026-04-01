package com.tneaguider.backend.service;

import com.tneaguider.backend.dto.RecommendationItemDto;
import com.tneaguider.backend.dto.RecommendationRequestDto;
import com.tneaguider.backend.dto.RecommendationResponseDto;
import com.tneaguider.backend.entity.College;
import com.tneaguider.backend.entity.District;
import com.tneaguider.backend.entity.Recommendation;
import com.tneaguider.backend.entity.Student;
import com.tneaguider.backend.repository.CollegeRepository;
import com.tneaguider.backend.repository.RecommendationRepository;
import com.tneaguider.backend.repository.StudentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final double MAX_FEES基准 = 150000;
    private static final double CUTOFF_WEIGHT = 0.5;
    private static final double PLACEMENT_WEIGHT = 0.3;
    private static final double FEES_WEIGHT = 0.2;

    private static final java.util.Map<String, String> DISTRICT_ALIASES;
    private static final java.util.Map<String, String> BRANCH_ALIASES;
    
    static {
        java.util.Map<String, String> districtMap = new java.util.HashMap<>();
        districtMap.put("trichy", "Tiruchirappalli");
        districtMap.put("tiruchirappalli", "Tiruchirappalli");
        districtMap.put("chennai", "Chennai");
        districtMap.put("coimbatore", "Coimbatore");
        districtMap.put("madurai", "Madurai");
        districtMap.put("salem", "Salem");
        districtMap.put("vellore", "Vellore");
        districtMap.put("tirunelveli", "Tirunelveli");
        districtMap.put("tanjore", "Thanjavur");
        districtMap.put("thanjavur", "Thanjavur");
        districtMap.put("dindigul", "Dindigul");
        districtMap.put("erode", "Erode");
        districtMap.put("namakkal", "Namakkal");
        districtMap.put("kanchipuram", "Kanchipuram");
        districtMap.put("chengalpattu", "Chengalpattu");
        districtMap.put("tiruvallur", "Tiruvallur");
        DISTRICT_ALIASES = java.util.Collections.unmodifiableMap(districtMap);
        
        java.util.Map<String, String> branchMap = new java.util.HashMap<>();
        branchMap.put("ai ds", "AI & DS");
        branchMap.put("aids", "AI & DS");
        branchMap.put("ai & ds", "AI & DS");
        branchMap.put("ai ml", "AI & ML");
        branchMap.put("aiml", "AI & ML");
        branchMap.put("ai & ml", "AI & ML");
        branchMap.put("cse", "CSE");
        branchMap.put("it", "IT");
        branchMap.put("ece", "ECE");
        branchMap.put("eee", "EEE");
        branchMap.put("mech", "MECH");
        branchMap.put("civil", "CIVIL");
        branchMap.put("computer science", "CSE");
        branchMap.put("information technology", "IT");
        branchMap.put("electronics", "ECE");
        branchMap.put("electrical", "EEE");
        branchMap.put("mechanical", "MECH");
        BRANCH_ALIASES = java.util.Collections.unmodifiableMap(branchMap);
    }

    private final CollegeRepository collegeRepository;
    private final StudentRepository studentRepository;
    private final RecommendationRepository recommendationRepository;

    public RecommendationService(CollegeRepository collegeRepository,
                                  StudentRepository studentRepository,
                                  RecommendationRepository recommendationRepository) {
        this.collegeRepository = collegeRepository;
        this.studentRepository = studentRepository;
        this.recommendationRepository = recommendationRepository;
    }

    @Transactional
    public RecommendationResponseDto generateRecommendations(RecommendationRequestDto request) {
        Double cutoffInput = request.getCutoff() != null ? request.getCutoff().doubleValue() : null;
        String categoryInput = request.getCategory();
        String branchInput = request.getBranch();
        String districtInput = request.getDistrict();
        Double budgetInput = request.getBudget() != null ? request.getBudget().doubleValue() : null;
        String tierInput = request.getPreference() != null ? request.getPreference() : null;

        double studentCutoff = cutoffInput != null ? cutoffInput : 190.0;
        String normalizedCategory = normalize(categoryInput);
        String normalizedBranch = resolveBranchAlias(normalize(branchInput));
        String normalizedDistrict = resolveDistrictAlias(normalize(districtInput));
        Double budget = budgetInput;
        String normalizedTier = normalize(tierInput);

        List<College> colleges = fetchWithFlexibleFilters(
            studentCutoff, normalizedDistrict, normalizedTier, budget
        );

        if (colleges.isEmpty()) {
            colleges = relaxAndFetch(studentCutoff);
        }

        if (colleges.isEmpty()) {
            colleges = fetchAllCollegesFallback();
        }

        if (colleges.isEmpty()) {
            throw new RuntimeException("No colleges found in database");
        }

        List<ScoredCollege> scoredList = colleges.stream()
            .map(college -> {
                double score = calculateSmartScore(studentCutoff, college, normalizedBranch, normalizedDistrict, budget);
                String classification = classifyCollege(studentCutoff, college.getCutoff());
                return new ScoredCollege(college, score, classification);
            })
            .sorted((a, b) -> Double.compare(b.score, a.score))
            .collect(Collectors.toList());

        List<ScoredCollege> dreamList = scoredList.stream()
            .filter(s -> "DREAM".equals(s.classification))
            .collect(Collectors.toList());
        List<ScoredCollege> moderateList = scoredList.stream()
            .filter(s -> "MODERATE".equals(s.classification))
            .collect(Collectors.toList());
        List<ScoredCollege> safeList = scoredList.stream()
            .filter(s -> "SAFE".equals(s.classification))
            .collect(Collectors.toList());

        if (dreamList.isEmpty() && !scoredList.isEmpty()) {
            int top30 = (int) (scoredList.size() * 0.3);
            dreamList = scoredList.subList(0, Math.min(top30, scoredList.size()));
            safeList = scoredList.subList(dreamList.size(), scoredList.size());
        }

        RecommendationResponseDto response = new RecommendationResponseDto();
        Student student = saveStudent(request, studentCutoff, normalizedCategory, normalizedBranch, normalizedDistrict, budget);
        List<Recommendation> recordsToPersist = new ArrayList<>();

        for (ScoredCollege sc : scoredList) {
            RecommendationItemDto item = mapToDto(sc.college, sc.score, sc.classification);
            switch (sc.classification) {
                case "DREAM" -> response.getDream().add(item);
                case "MODERATE" -> response.getModerate().add(item);
                case "SAFE" -> response.getSafe().add(item);
            }
            Recommendation rec = buildRecommendation(student, sc.college, (int) sc.score, sc.classification);
            recordsToPersist.add(rec);
        }

        trimResults(response);
        recommendationRepository.saveAll(recordsToPersist);

        return response;
    }

    private List<College> fetchWithFlexibleFilters(Double cutoff, String district, String tier, Double maxFees) {
        Double effectiveMaxFees = maxFees != null ? maxFees : null;
        
        if (district == null && tier == null && maxFees == null) {
            return fetchCollegesBasic(cutoff);
        }

        List<Object[]> results = collegeRepository.findWithFilters(
            cutoff,
            district,
            tier,
            effectiveMaxFees
        );

        return mapToColleges(results);
    }

    private List<College> fetchCollegesBasic(Double cutoff) {
        if (cutoff != null) {
            List<Object[]> results = collegeRepository.findAllWithDetailsByCutoff(cutoff);
            return mapToColleges(results);
        }
        List<Object[]> results = collegeRepository.findAllWithDetails();
        return mapToColleges(results);
    }

    private List<College> relaxAndFetch(double cutoff) {
        double relaxedCutoff = cutoff + 10;
        List<Object[]> results = collegeRepository.findAllWithDetailsByCutoff(relaxedCutoff);
        return mapToColleges(results);
    }

    private List<College> fetchAllCollegesFallback() {
        List<Object[]> results = collegeRepository.findAllWithDetails();
        return mapToColleges(results);
    }

    private List<College> mapToColleges(List<Object[]> results) {
        List<College> colleges = new ArrayList<>();
        for (Object[] row : results) {
            College college = new College();
            college.setId(((Number) row[0]).longValue());
            college.setName((String) row[1]);
            college.setBranch((String) row[2]);
            college.setCategory((String) row[3]);
            
            District district = new District();
            district.setName((String) row[4]);
            college.setDistrict(district);
            
            college.setCutoff(toDouble(row[5]));
            college.setFees(toDouble(row[6]));
            college.setPlacementRate(toDouble(row[7]));
            college.setTier((String) row[8]);
            colleges.add(college);
        }
        return colleges;
    }

    private Double toDouble(Object obj) {
        if (obj == null) return 0.0;
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        return Double.parseDouble(obj.toString());
    }

    private double calculateSmartScore(double studentCutoff, College college, String targetBranch, 
                                       String targetDistrict, Double budget) {
        double cutoff = college.getCutoff() != null ? college.getCutoff() : 0;
        double placement = college.getPlacementRate() != null ? college.getPlacementRate() : 0;
        double fees = college.getFees() != null ? college.getFees() : MAX_FEES基准;

        double normalizedCutoff = (cutoff / 200.0) * 100;
        double normalizedPlacement = placement;
        double normalizedFees = ((MAX_FEES基准 - fees) / MAX_FEES基准) * 100;

        double score = (normalizedCutoff * CUTOFF_WEIGHT) + 
                       (normalizedPlacement * PLACEMENT_WEIGHT) + 
                       (normalizedFees * FEES_WEIGHT);

        if (targetBranch != null && college.getBranch() != null) {
            if (targetBranch.equalsIgnoreCase(college.getBranch())) {
                score += 15;
            }
        }

        if (targetDistrict != null && college.getDistrictName() != null) {
            if (targetDistrict.equalsIgnoreCase(college.getDistrictName())) {
                score += 20;
            }
        }

        if (budget != null && fees <= budget) {
            score += 10;
        }

        if ("Tier 1".equalsIgnoreCase(college.getTier())) {
            score += 10;
        } else if ("Tier 2".equalsIgnoreCase(college.getTier())) {
            score += 5;
        }

        return score;
    }

    private String classifyCollege(double studentCutoff, Double collegeCutoff) {
        if (collegeCutoff == null) return "SAFE";
        
        if (collegeCutoff >= studentCutoff) {
            return "DREAM";
        } else if (collegeCutoff >= studentCutoff - 5) {
            return "MODERATE";
        } else {
            return "SAFE";
        }
    }

    private void trimResults(RecommendationResponseDto response) {
        int maxPerCategory = 15;
        
        if (response.getDream().size() > maxPerCategory) {
            response.getDream().subList(maxPerCategory, response.getDream().size()).clear();
        }
        if (response.getModerate().size() > maxPerCategory) {
            response.getModerate().subList(maxPerCategory, response.getModerate().size()).clear();
        }
        if (response.getSafe().size() > maxPerCategory) {
            response.getSafe().subList(maxPerCategory, response.getSafe().size()).clear();
        }
    }

    public List<RecommendationItemDto> compareColleges(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("At least one college id is required for comparison.");
        }

        List<Long> uniqueValidIds = ids.stream()
            .filter(id -> id != null && id > 0)
            .distinct()
            .collect(Collectors.toList());

        if (uniqueValidIds.isEmpty()) {
            throw new IllegalArgumentException("At least one college id is required.");
        }

        if (uniqueValidIds.size() > 3) {
            throw new IllegalArgumentException("You can compare maximum 3 colleges.");
        }

        List<College> colleges = collegeRepository.findAllById(uniqueValidIds);
        
        List<RecommendationItemDto> result = new ArrayList<>();
        for (Long id : uniqueValidIds) {
            colleges.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .ifPresent(college -> result.add(mapToDto(college, null, null)));
        }

        return result;
    }

    private Student saveStudent(RecommendationRequestDto request, double cutoff, String category, 
                                String branch, String district, Double budget) {
        Student student = new Student();
        student.setCutoff(cutoff);
        student.setCategory(category != null ? category : "BC");
        student.setBranch(branch != null ? branch : "ANY");
        student.setDistrict(district);
        student.setBudget(budget != null ? budget : 0.0);
        student.setCreatedAt(LocalDateTime.now());
        return studentRepository.save(student);
    }

    private RecommendationItemDto mapToDto(College college, Double score, String classification) {
        RecommendationItemDto dto = new RecommendationItemDto();
        dto.setCollegeId(college.getId());
        dto.setCollegeName(college.getName());
        dto.setDistrict(college.getDistrictName());
        dto.setCutoff(college.getCutoff());
        dto.setFees(college.getFees());
        dto.setPlacementRate(college.getPlacementRate());
        dto.setTier(college.getTier());
        dto.setFinalScore(score != null ? score : 0.0);
        return dto;
    }

    private Recommendation buildRecommendation(Student student, College college, int score, String classification) {
        Recommendation rec = new Recommendation();
        rec.setStudent(student);
        rec.setCollege(college);
        rec.setFinalScore((double) score);
        rec.setClassification(classification);
        rec.setCreatedAt(LocalDateTime.now());
        return rec;
    }

    private String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private String resolveDistrictAlias(String district) {
        if (district == null) return null;
        String lower = district.toLowerCase().trim();
        String resolved = DISTRICT_ALIASES.get(lower);
        return resolved != null ? resolved : district;
    }

    private String resolveBranchAlias(String branch) {
        if (branch == null) return null;
        String lower = branch.toLowerCase().trim();
        String resolved = BRANCH_ALIASES.get(lower);
        return resolved != null ? resolved : branch;
    }

    private static class ScoredCollege {
        final College college;
        final double score;
        final String classification;

        ScoredCollege(College college, double score, String classification) {
            this.college = college;
            this.score = score;
            this.classification = classification;
        }
    }
}
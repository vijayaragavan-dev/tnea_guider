package com.tneaguider.backend.controller;

import com.tneaguider.backend.repository.BranchRepository;
import com.tneaguider.backend.repository.CategoryRepository;
import com.tneaguider.backend.repository.DistrictRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MasterDataController {

    private final DistrictRepository districtRepository;
    private final BranchRepository branchRepository;
    private final CategoryRepository categoryRepository;

    public MasterDataController(DistrictRepository districtRepository,
                                BranchRepository branchRepository,
                                CategoryRepository categoryRepository) {
        this.districtRepository = districtRepository;
        this.branchRepository = branchRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/districts")
    public ResponseEntity<List<String>> getDistricts() {
        return ResponseEntity.ok(districtRepository.findAllNames());
    }

    @GetMapping("/branches")
    public ResponseEntity<List<String>> getBranches() {
        return ResponseEntity.ok(branchRepository.findAllNames());
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(categoryRepository.findAllNames());
    }

    @GetMapping("/master-data")
    public ResponseEntity<Map<String, List<String>>> getAllMasterData() {
        return ResponseEntity.ok(Map.of(
            "districts", districtRepository.findAllNames(),
            "branches", branchRepository.findAllNames(),
            "categories", categoryRepository.findAllNames()
        ));
    }
}
package com.tneaguider.backend.repository;

import com.tneaguider.backend.entity.Weight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeightRepository extends JpaRepository<Weight, Long> {
    Optional<Weight> findByWeightKeyIgnoreCase(String weightKey);
}

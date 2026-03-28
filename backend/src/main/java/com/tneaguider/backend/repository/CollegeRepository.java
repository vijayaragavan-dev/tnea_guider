package com.tneaguider.backend.repository;

import com.tneaguider.backend.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollegeRepository extends JpaRepository<College, Long> {
}

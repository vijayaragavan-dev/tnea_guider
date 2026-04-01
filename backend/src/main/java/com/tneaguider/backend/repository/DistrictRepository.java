package com.tneaguider.backend.repository;

import com.tneaguider.backend.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistrictRepository extends JpaRepository<District, Integer> {

    @Query("SELECT d.name FROM District d ORDER BY d.name")
    List<String> findAllNames();
}
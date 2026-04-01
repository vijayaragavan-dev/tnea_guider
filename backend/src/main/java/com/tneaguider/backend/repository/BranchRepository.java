package com.tneaguider.backend.repository;

import com.tneaguider.backend.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Integer> {

    @Query("SELECT b.name FROM Branch b ORDER BY b.name")
    List<String> findAllNames();
}
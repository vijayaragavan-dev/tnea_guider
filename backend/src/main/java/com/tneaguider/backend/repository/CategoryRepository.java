package com.tneaguider.backend.repository;

import com.tneaguider.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    @Query("SELECT c.name FROM Category c ORDER BY c.name")
    List<String> findAllNames();
}
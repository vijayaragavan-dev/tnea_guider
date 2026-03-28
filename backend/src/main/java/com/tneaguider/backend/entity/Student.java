package com.tneaguider.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double cutoff;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String branch;

    @Column(nullable = false)
    private Double budget;

    private String district;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Student() {
    }

    public Student(Long id, Double cutoff, String category, String branch, Double budget, String district,
                   LocalDateTime createdAt) {
        this.id = id;
        this.cutoff = cutoff;
        this.category = category;
        this.branch = branch;
        this.budget = budget;
        this.district = district;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getCutoff() {
        return cutoff;
    }

    public void setCutoff(Double cutoff) {
        this.cutoff = cutoff;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

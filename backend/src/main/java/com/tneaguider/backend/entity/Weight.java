package com.tneaguider.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "weights")
public class Weight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "weight_key", nullable = false, unique = true)
    private String weightKey;

    @Column(name = "weight_value", nullable = false)
    private Double weightValue;

    public Weight() {
    }

    public Weight(Long id, String weightKey, Double weightValue) {
        this.id = id;
        this.weightKey = weightKey;
        this.weightValue = weightValue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWeightKey() {
        return weightKey;
    }

    public void setWeightKey(String weightKey) {
        this.weightKey = weightKey;
    }

    public Double getWeightValue() {
        return weightValue;
    }

    public void setWeightValue(Double weightValue) {
        this.weightValue = weightValue;
    }
}

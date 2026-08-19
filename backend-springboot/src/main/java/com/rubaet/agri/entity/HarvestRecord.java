package com.rubaet.agri.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "harvest_records")
public class HarvestRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "crop_name", nullable = false, length = 120)
    private String cropName;

    @Column(length = 60)
    private String season;

    @Column(name = "harvest_year", nullable = false)
    private int year;

    @Column(name = "actual_yield_kg", nullable = false)
    private double actualYieldKg;

    @Column(name = "revenue_usd")
    private double revenueUsd = 0.0;

    @Column(length = 500)
    private String notes;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private Instant recordedAt;

    @PrePersist
    void onCreate() { this.recordedAt = Instant.now(); }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }
    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public double getActualYieldKg() { return actualYieldKg; }
    public void setActualYieldKg(double actualYieldKg) { this.actualYieldKg = actualYieldKg; }
    public double getRevenueUsd() { return revenueUsd; }
    public void setRevenueUsd(double revenueUsd) { this.revenueUsd = revenueUsd; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Instant getRecordedAt() { return recordedAt; }
}

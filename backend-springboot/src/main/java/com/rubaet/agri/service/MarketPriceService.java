package com.rubaet.agri.service;

import org.springframework.stereotype.Service;
import java.util.Map;

/**
 * Provides estimated crop market prices per kilogram.
 * Based on 2024 average South Asian commodity prices.
 * Can be replaced with a live API integration later.
 */
@Service
public class MarketPriceService {

    private static final Map<String, Double> PRICES_PER_KG = Map.ofEntries(
        Map.entry("rice",         0.45),
        Map.entry("wheat",        0.30),
        Map.entry("maize",        0.28),
        Map.entry("jute",         0.35),
        Map.entry("soybean",      0.55),
        Map.entry("groundnut",    0.80),
        Map.entry("mustard",      0.60),
        Map.entry("lentil",       1.10),
        Map.entry("potato",       0.25),
        Map.entry("sweet potato", 0.30),
        Map.entry("onion",        0.40),
        Map.entry("garlic",       2.50),
        Map.entry("tomato",       0.50),
        Map.entry("spinach",      0.60),
        Map.entry("carrot",       0.45),
        Map.entry("corn",         0.28)
    );

    /** Estimated yield in kg per hectare for a good season. */
    private static final Map<String, Double> YIELD_PER_HECTARE = Map.ofEntries(
        Map.entry("rice",         4500.0),
        Map.entry("wheat",        3200.0),
        Map.entry("maize",        5000.0),
        Map.entry("jute",         2800.0),
        Map.entry("soybean",      2500.0),
        Map.entry("groundnut",    2000.0),
        Map.entry("mustard",      1500.0),
        Map.entry("lentil",       1200.0),
        Map.entry("potato",       20000.0),
        Map.entry("sweet potato", 15000.0),
        Map.entry("onion",        12000.0),
        Map.entry("garlic",       8000.0),
        Map.entry("tomato",       25000.0),
        Map.entry("spinach",      10000.0),
        Map.entry("carrot",       18000.0),
        Map.entry("corn",         5500.0)
    );

    /** Returns price per kg in USD, or a default 0.40 if crop not found. */
    public double getPricePerKg(String crop) {
        return PRICES_PER_KG.getOrDefault(crop.toLowerCase().trim(), 0.40);
    }

    /** Returns estimated yield in kg/hectare, or 3000 as default. */
    public double getYieldPerHectare(String crop) {
        return YIELD_PER_HECTARE.getOrDefault(crop.toLowerCase().trim(), 3000.0);
    }

    public boolean cropIsKnown(String crop) {
        return PRICES_PER_KG.containsKey(crop.toLowerCase().trim());
    }
}

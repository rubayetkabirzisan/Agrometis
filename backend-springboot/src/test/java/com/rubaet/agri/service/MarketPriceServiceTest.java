package com.rubaet.agri.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MarketPriceService}.
 * These are pure logic tests — no Spring context required.
 */
class MarketPriceServiceTest {

    private final MarketPriceService service = new MarketPriceService();

    @Test
    @DisplayName("Known crop returns correct price per kg")
    void knownCropReturnsCorrectPrice() {
        assertEquals(0.45, service.getPricePerKg("rice"));
        assertEquals(2.50, service.getPricePerKg("garlic"));
    }

    @Test
    @DisplayName("Price lookup is case-insensitive and trims whitespace")
    void priceLookupIsCaseInsensitive() {
        assertEquals(0.45, service.getPricePerKg("  Rice "));
        assertEquals(0.28, service.getPricePerKg("MAIZE"));
    }

    @Test
    @DisplayName("Unknown crop returns default price of 0.40")
    void unknownCropReturnsDefault() {
        assertEquals(0.40, service.getPricePerKg("dragon fruit"));
    }

    @Test
    @DisplayName("Known crop returns correct yield per hectare")
    void knownCropReturnsCorrectYield() {
        assertEquals(4500.0, service.getYieldPerHectare("rice"));
        assertEquals(20000.0, service.getYieldPerHectare("potato"));
    }

    @Test
    @DisplayName("Unknown crop returns default yield of 3000 kg/hectare")
    void unknownCropReturnsDefaultYield() {
        assertEquals(3000.0, service.getYieldPerHectare("quinoa"));
    }

    @Test
    @DisplayName("cropIsKnown returns true for known crops, false for unknown")
    void cropIsKnown() {
        assertTrue(service.cropIsKnown("rice"));
        assertTrue(service.cropIsKnown("  WHEAT  "));
        assertFalse(service.cropIsKnown("dragon fruit"));
    }
}

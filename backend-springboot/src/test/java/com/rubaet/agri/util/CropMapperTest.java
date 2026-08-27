package com.rubaet.agri.util;

import com.rubaet.agri.dto.CropCreateRequest;
import com.rubaet.agri.dto.CropUpdateRequest;
import com.rubaet.agri.entity.Crop;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CropMapper}.
 * Verifies entity ↔ DTO mapping logic.
 */
class CropMapperTest {

    @Test
    @DisplayName("toEntity maps CropCreateRequest fields correctly and trims whitespace")
    void toEntityMapsFieldsAndTrims() {
        CropCreateRequest req = new CropCreateRequest();
        req.setName("  Rice  ");
        req.setSeason(" Kharif ");
        req.setDescription("  Staple grain crop  ");

        Crop crop = CropMapper.toEntity(req);

        assertEquals("Rice", crop.getName());
        assertEquals("Kharif", crop.getSeason());
        assertEquals("Staple grain crop", crop.getDescription());
    }

    @Test
    @DisplayName("toEntity sets null season/description when input is blank")
    void toEntityHandlesBlankFields() {
        CropCreateRequest req = new CropCreateRequest();
        req.setName("Wheat");
        req.setSeason("   ");
        req.setDescription(null);

        Crop crop = CropMapper.toEntity(req);

        assertEquals("Wheat", crop.getName());
        assertNull(crop.getSeason(), "Blank season should become null");
        assertNull(crop.getDescription(), "Null description should stay null");
    }

    @Test
    @DisplayName("applyUpdate only modifies non-null fields")
    void applyUpdateOnlyModifiesNonNullFields() {
        Crop crop = new Crop();
        crop.setName("Rice");
        crop.setSeason("Kharif");
        crop.setDescription("Original");

        CropUpdateRequest req = new CropUpdateRequest();
        req.setName("Updated Rice");
        req.setSeason(null);       // should not change
        req.setDescription(null);  // should not change

        CropMapper.applyUpdate(crop, req);

        assertEquals("Updated Rice", crop.getName());
        assertEquals("Kharif", crop.getSeason(), "Season should remain unchanged");
        assertEquals("Original", crop.getDescription(), "Description should remain unchanged");
    }

    @Test
    @DisplayName("toResponse maps all entity fields to response DTO")
    void toResponseMapsAllFields() {
        Crop crop = new Crop();
        crop.setId(42L);
        crop.setName("Tomato");
        crop.setSeason("Summer");
        crop.setDescription("A red fruit used as a vegetable");

        var response = CropMapper.toResponse(crop);

        assertEquals(42L, response.getId());
        assertEquals("Tomato", response.getName());
        assertEquals("Summer", response.getSeason());
        assertEquals("A red fruit used as a vegetable", response.getDescription());
    }
}

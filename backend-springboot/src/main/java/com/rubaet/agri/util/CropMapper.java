package com.rubaet.agri.util;

import com.rubaet.agri.dto.CropCreateRequest;
import com.rubaet.agri.dto.CropResponse;
import com.rubaet.agri.dto.CropUpdateRequest;
import com.rubaet.agri.entity.Crop;

public final class CropMapper {

    private CropMapper() {}

    public static Crop toEntity(CropCreateRequest req) {
        Crop c = new Crop();
        c.setName(req.getName().trim());
        c.setSeason(safeTrim(req.getSeason()));
        c.setDescription(safeTrim(req.getDescription()));
        return c;
    }

    public static void applyUpdate(Crop c, CropUpdateRequest req) {
        if (req.getName() != null) c.setName(req.getName().trim());
        if (req.getSeason() != null) c.setSeason(safeTrim(req.getSeason()));
        if (req.getDescription() != null) c.setDescription(safeTrim(req.getDescription()));
    }

    public static CropResponse toResponse(Crop c) {
        CropResponse r = new CropResponse();
        r.setId(c.getId());
        r.setName(c.getName());
        r.setSeason(c.getSeason());
        r.setDescription(c.getDescription());
        r.setCreatedAt(c.getCreatedAt());
        r.setUpdatedAt(c.getUpdatedAt());
        return r;
    }

    private static String safeTrim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}

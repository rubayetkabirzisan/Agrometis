package com.rubaet.agri.service;

import com.rubaet.agri.dto.CropCreateRequest;
import com.rubaet.agri.dto.CropResponse;
import com.rubaet.agri.dto.CropUpdateRequest;

import java.util.List;

public interface CropService {
    CropResponse create(CropCreateRequest req);

    List<CropResponse> getAll();

    CropResponse getById(Long id);
    
    CropResponse getByName(String name);

    CropResponse update(Long id, CropUpdateRequest req);

    void delete(Long id);
}

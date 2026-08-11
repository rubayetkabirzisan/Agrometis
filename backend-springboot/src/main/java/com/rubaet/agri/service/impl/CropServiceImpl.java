package com.rubaet.agri.service.impl;

import com.rubaet.agri.dto.CropCreateRequest;
import com.rubaet.agri.dto.CropResponse;
import com.rubaet.agri.dto.CropUpdateRequest;
import com.rubaet.agri.entity.Crop;
import com.rubaet.agri.exception.DuplicateResourceException;
import com.rubaet.agri.exception.ResourceNotFoundException;
import com.rubaet.agri.repository.CropRepository;
import com.rubaet.agri.service.CropService;
import com.rubaet.agri.util.CropMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CropServiceImpl implements CropService {

    private final CropRepository repo;

    public CropServiceImpl(CropRepository repo) {
        this.repo = repo;
    }

    @Override
    public CropResponse create(CropCreateRequest req) {
        String name = req.getName().trim();
        if (repo.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Crop with the same name already exists");
        }
        Crop saved = repo.save(CropMapper.toEntity(req));
        return CropMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CropResponse> getAll() {
        return repo.findAll().stream().map(CropMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CropResponse getById(Long id) {
        Crop c = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Crop not found with id=" + id));
        return CropMapper.toResponse(c);
    }

    @Override
    @Transactional(readOnly = true)
    public CropResponse getByName(String name) {
        Crop c = repo.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Crop not found with name=" + name));
        return CropMapper.toResponse(c);
    }

    @Override
    public CropResponse update(Long id, CropUpdateRequest req) {
        Crop c = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Crop not found with id=" + id));

        if (req.getName() != null) {
            String newName = req.getName().trim();
            if (!newName.equalsIgnoreCase(c.getName()) && repo.existsByNameIgnoreCase(newName)) {
                throw new DuplicateResourceException("Crop with the same name already exists");
            }
        }

        CropMapper.applyUpdate(c, req);
        Crop saved = repo.save(c);
        return CropMapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Crop not found with id=" + id);
        }
        repo.deleteById(id);
    }
}

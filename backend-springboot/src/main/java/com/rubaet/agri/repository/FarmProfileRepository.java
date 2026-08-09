package com.rubaet.agri.repository;

import com.rubaet.agri.entity.FarmProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FarmProfileRepository extends JpaRepository<FarmProfile, Long> {
    Optional<FarmProfile> findByUserId(Long userId);
}

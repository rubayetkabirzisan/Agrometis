package com.rubaet.agri.repository;

import com.rubaet.agri.entity.HarvestRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HarvestRecordRepository extends JpaRepository<HarvestRecord, Long> {
    List<HarvestRecord> findByUserIdOrderByYearDescSeasonDesc(Long userId);
}

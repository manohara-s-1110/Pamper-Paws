package com.pamperpaw.visit.repository;

import com.pamperpaw.visit.entity.VetLeave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VetLeaveRepository extends JpaRepository<VetLeave, Long> {
    List<VetLeave> findByVetIdOrderByDateAsc(Long vetId);
    Optional<VetLeave> findByVetIdAndDate(Long vetId, String date);
    boolean existsByVetIdAndDate(Long vetId, String date);
}

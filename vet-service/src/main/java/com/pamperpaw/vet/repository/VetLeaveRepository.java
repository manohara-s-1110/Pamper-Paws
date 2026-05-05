package com.pamperpaw.vet.repository;

import com.pamperpaw.vet.entity.VetLeave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VetLeaveRepository extends JpaRepository<VetLeave, Long> {

    List<VetLeave> findByVetIdOrderByLeaveDateAsc(Long vetId);

    Optional<VetLeave> findByVetIdAndLeaveDate(Long vetId, String leaveDate);

    boolean existsByVetIdAndLeaveDate(Long vetId, String leaveDate);
}

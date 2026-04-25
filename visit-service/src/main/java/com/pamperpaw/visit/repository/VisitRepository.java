package com.pamperpaw.visit.repository;

import com.pamperpaw.visit.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    // 🔥 ADD THIS (required for fetching user appointments)
    List<Visit> findByCustomerId(Long customerId);
    List<Visit> findByVetId(Long vetId);
    List<Visit> findByPetId(Long petId);
    List<Visit> findByVetIdAndVisitDate(Long vetId, String visitDate);
}

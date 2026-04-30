package com.pamperpaw.admin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.pamperpaw.admin.dto.VisitDTO;

import java.util.List;

@FeignClient(name = "visit-service")
public interface VisitClient {

    @GetMapping("/visit")
    List<VisitDTO> getAllVisits();

    @GetMapping("/visit/{id}")
    VisitDTO getVisitById(@PathVariable("id") Long id);

    @DeleteMapping("/visit/{id}")
    void deleteVisit(@PathVariable("id") Long id);
}

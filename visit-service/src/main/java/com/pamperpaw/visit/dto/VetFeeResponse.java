package com.pamperpaw.visit.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VetFeeResponse {
    private Long vetId;
    private BigDecimal consultationFee;
}

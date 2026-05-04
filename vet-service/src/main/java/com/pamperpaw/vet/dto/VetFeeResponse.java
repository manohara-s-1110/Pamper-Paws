package com.pamperpaw.vet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class VetFeeResponse {
    private Long vetId;
    private BigDecimal consultationFee;
}

package com.pamperpaw.admin.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitDTO {

    private Long id;
    private Long customerId;
    private Long vetId;
    private String visitDate;
    private String reason;
}
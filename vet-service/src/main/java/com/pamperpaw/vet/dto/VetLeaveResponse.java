package com.pamperpaw.vet.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VetLeaveResponse {

    private Long id;
    private Long vetId;
    private String date;
}

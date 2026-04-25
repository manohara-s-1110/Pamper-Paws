package com.pamperpaw.admin.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VetDTO {
    private Long id;
    private String username;
    private String name;
    private String specialization;
    private int experience;
    private String phone;
    private String email;
    private String clinicAddress;
    private String availableDays;
    private String availableTime;
}

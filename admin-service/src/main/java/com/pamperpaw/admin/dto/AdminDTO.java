package com.pamperpaw.admin.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDTO {

    private Long id;

   
    private String name;

   
    private String email;

   
    private String password;

   
    private String role;
}

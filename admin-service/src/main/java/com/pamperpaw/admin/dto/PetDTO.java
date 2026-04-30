package com.pamperpaw.admin.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetDTO {
    private Long id;
    private String name;
    private String type;
    private int age;
    private String imageUrl;
}

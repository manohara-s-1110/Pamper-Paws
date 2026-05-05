package com.pamperpaw.visit.dto;

import lombok.Data;

@Data
public class PetDTO {
    private Long id;
    private String name;
    private String type;
    private int age;
    private String imageUrl;
}

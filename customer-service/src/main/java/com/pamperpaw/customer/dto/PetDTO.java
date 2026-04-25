package com.pamperpaw.customer.dto;

import jakarta.validation.constraints.*;

public class PetDTO {

    private Long id;

    @NotBlank(message = "Pet name is required")
    private String name;

    @NotBlank(message = "Pet type is required")
    private String type;

    @Min(value = 0, message = "Age cannot be negative")
    private int age;

    private String imageUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}

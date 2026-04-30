package com.pamperpaw.customer.service;

import com.pamperpaw.customer.dto.PetDTO;
import com.pamperpaw.customer.entity.Pet;

public class PetMapper {

    public static Pet toEntity(PetDTO dto) {
        Pet pet = new Pet();

        pet.setId(dto.getId());
        pet.setName(dto.getName());
        pet.setType(dto.getType());
        pet.setAge(dto.getAge());
        pet.setImageUrl(dto.getImageUrl());

        return pet;
    }

    public static PetDTO toDTO(Pet pet) {
        PetDTO dto = new PetDTO();

        dto.setId(pet.getId());
        dto.setName(pet.getName());
        dto.setType(pet.getType());
        dto.setAge(pet.getAge());
        dto.setImageUrl(pet.getImageUrl());

        return dto;
    }
}

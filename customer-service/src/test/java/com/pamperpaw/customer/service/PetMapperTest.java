package com.pamperpaw.customer.service;

import com.pamperpaw.customer.dto.PetDTO;
import com.pamperpaw.customer.entity.Pet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PetMapperTest {

    @Test
    void supportsInstantiation() {
        assertDoesNotThrow(PetMapper::new);
    }

    @Test
    void mapsDtoToEntityAndBack() {
        PetDTO dto = new PetDTO();
        dto.setId(1L);
        dto.setName("Bruno");
        dto.setType("Dog");
        dto.setAge(2);

        Pet entity = PetMapper.toEntity(dto);
        PetDTO mappedDto = PetMapper.toDTO(entity);

        assertEquals(1L, entity.getId());
        assertEquals("Bruno", entity.getName());
        assertEquals("Dog", entity.getType());
        assertEquals("Dog", mappedDto.getType());
        assertEquals(2, mappedDto.getAge());
        assertEquals(1L, mappedDto.getId());
        assertEquals("Bruno", mappedDto.getName());
    }
}

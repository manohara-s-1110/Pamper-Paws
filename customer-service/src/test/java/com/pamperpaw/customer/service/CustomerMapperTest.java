package com.pamperpaw.customer.service;

import com.pamperpaw.customer.dto.CustomerDTO;
import com.pamperpaw.customer.entity.Customer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomerMapperTest {

    @Test
    void supportsInstantiation() {
        assertDoesNotThrow(CustomerMapper::new);
    }

    @Test
    void mapsDtoToEntityAndBack() {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(1L);
        dto.setName("Manu");
        dto.setEmail("manu@test.com");
        dto.setPhone("9876543210");
        dto.setAddress("Chennai");

        Customer entity = CustomerMapper.toEntity(dto);
        CustomerDTO mappedDto = CustomerMapper.toDTO(entity);

        assertEquals(1L, entity.getId());
        assertEquals("Manu", entity.getName());
        assertEquals("9876543210", entity.getPhone());
        assertEquals("manu@test.com", mappedDto.getEmail());
        assertEquals("Chennai", mappedDto.getAddress());
        assertEquals(1L, mappedDto.getId());
        assertEquals("9876543210", mappedDto.getPhone());
    }
}

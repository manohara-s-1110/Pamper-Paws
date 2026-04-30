package com.pamperpaw.customer.service.impl;

import com.pamperpaw.customer.dto.PetDTO;
import com.pamperpaw.customer.entity.Customer;
import com.pamperpaw.customer.entity.Pet;
import com.pamperpaw.customer.exception.ResourceNotFoundException;
import com.pamperpaw.customer.repository.CustomerRepository;
import com.pamperpaw.customer.repository.PetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetServiceImplTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private PetServiceImpl petService;

    @Test
    void addPetSavesForExistingCustomer() {
        PetDTO dto = new PetDTO();
        dto.setName("Paw");
        dto.setType("Dog");
        dto.setAge(2);

        Customer customer = new Customer();
        customer.setId(1L);

        Pet savedPet = new Pet();
        savedPet.setId(10L);
        savedPet.setName(dto.getName());
        savedPet.setType(dto.getType());
        savedPet.setAge(dto.getAge());

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(petRepository.save(any(Pet.class))).thenReturn(savedPet);

        PetDTO response = petService.addPet(1L, dto);

        assertEquals(10L, response.getId());
        assertEquals("Paw", response.getName());
    }

    @Test
    void addPetThrowsWhenCustomerMissing() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> petService.addPet(1L, new PetDTO()));
    }

    @Test
    void getPetsByCustomerMapsExistingPets() {
        Pet pet = new Pet();
        pet.setId(5L);
        pet.setName("Kitty");
        pet.setType("Cat");
        pet.setAge(3);

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setPets(List.of(pet));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        List<PetDTO> response = petService.getPetsByCustomer(1L);

        assertEquals(1, response.size());
        assertEquals("Kitty", response.get(0).getName());
    }
}

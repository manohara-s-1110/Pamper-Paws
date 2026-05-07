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
import static org.mockito.Mockito.verify;
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

        when(customerRepository.existsById(1L)).thenReturn(true);
        when(petRepository.findByCustomerId(1L)).thenReturn(List.of(pet));

        List<PetDTO> response = petService.getPetsByCustomer(1L);

        assertEquals(1, response.size());
        assertEquals("Kitty", response.get(0).getName());
    }

    @Test
    void getPetsByCustomerThrowsWhenCustomerMissing() {
        when(customerRepository.existsById(42L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> petService.getPetsByCustomer(42L));
    }

    @Test
    void updatePetUpdatesExistingPet() {
        Pet pet = new Pet();
        pet.setId(7L);
        pet.setName("Old");

        PetDTO dto = new PetDTO();
        dto.setName("Buddy");
        dto.setType("Dog");
        dto.setAge(4);
        dto.setImageUrl("dog.png");

        when(petRepository.findById(7L)).thenReturn(Optional.of(pet));
        when(petRepository.save(pet)).thenReturn(pet);

        PetDTO response = petService.updatePet(7L, dto);

        assertEquals("Buddy", response.getName());
        assertEquals("Dog", response.getType());
        assertEquals(4, response.getAge());
    }

    @Test
    void updatePetThrowsWhenMissing() {
        when(petRepository.findById(8L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> petService.updatePet(8L, new PetDTO()));
    }

    @Test
    void getPetByIdReturnsPet() {
        Pet pet = new Pet();
        pet.setId(9L);
        pet.setName("Max");
        pet.setType("Dog");

        when(petRepository.findById(9L)).thenReturn(Optional.of(pet));

        assertEquals("Max", petService.getPetById(9L).getName());
    }

    @Test
    void getPetByIdThrowsWhenMissing() {
        when(petRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> petService.getPetById(9L));
    }

    @Test
    void deletePetDeletesExistingPet() {
        Pet pet = new Pet();
        pet.setId(10L);

        when(petRepository.findById(10L)).thenReturn(Optional.of(pet));

        petService.deletePet(10L);

        verify(petRepository).delete(pet);
    }

    @Test
    void deletePetThrowsWhenMissing() {
        when(petRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> petService.deletePet(10L));
    }
}

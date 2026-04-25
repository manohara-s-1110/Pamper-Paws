package com.pamperpaw.customer.service.impl;

import com.pamperpaw.customer.dto.PetDTO;
import com.pamperpaw.customer.entity.Customer;
import com.pamperpaw.customer.entity.Pet;
import com.pamperpaw.customer.exception.ResourceNotFoundException;
import com.pamperpaw.customer.repository.CustomerRepository;
import com.pamperpaw.customer.repository.PetRepository;
import com.pamperpaw.customer.service.PetMapper;
import com.pamperpaw.customer.service.PetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PetServiceImpl implements PetService {

    private final PetRepository petRepo;
    private final CustomerRepository customerRepo;

    // ✅ ADD PET
    @Override
    public PetDTO addPet(Long customerId, PetDTO dto) {

        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer not found with id: " + customerId));

        Pet pet = PetMapper.toEntity(dto);
        pet.setCustomer(customer);

        Pet saved = petRepo.save(pet);

        log.info("Added pet with id={} for customerId={}", saved.getId(), customerId);

        return PetMapper.toDTO(saved);
    }

    // ✅ GET PETS BY CUSTOMER (FIXED)
    @Override
    public List<PetDTO> getPetsByCustomer(Long customerId) {

        // 🔥 OPTIONAL CHECK (good practice)
        if (!customerRepo.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found with id: " + customerId);
        }

        return petRepo.findByCustomerId(customerId)
                .stream()
                .map(PetMapper::toDTO)
                .toList();
    }
    
    @Override
    public PetDTO updatePet(Long petId, PetDTO dto) {

        Pet pet = petRepo.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet not found with id: " + petId));

        pet.setName(dto.getName());
        pet.setType(dto.getType());
        pet.setAge(dto.getAge());
        pet.setImageUrl(dto.getImageUrl());

        Pet updated = petRepo.save(pet);

        log.info("Updated pet with id={}", petId);

        return PetMapper.toDTO(updated);
    }

    @Override
    public void deletePet(Long petId) {

        Pet pet = petRepo.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet not found with id: " + petId));

        petRepo.delete(pet);

        log.info("Deleted pet with id={}", petId);
    }
}

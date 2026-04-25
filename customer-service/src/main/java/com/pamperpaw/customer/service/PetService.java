package com.pamperpaw.customer.service;

import java.util.List;
import com.pamperpaw.customer.dto.PetDTO;

public interface PetService {

    PetDTO addPet(Long customerId, PetDTO dto);

    PetDTO updatePet(Long petId, PetDTO dto);
    void deletePet(Long petId);
    List<PetDTO> getPetsByCustomer(Long customerId);
}
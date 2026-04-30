package com.pamperpaw.vet.repository;



import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pamperpaw.vet.entity.Vet;

@Repository
public interface VetRepository extends JpaRepository<Vet, Long> {
	
	List<Vet> findBySpecialization(String specialization);

	List<Vet> findByClinicAddress(String clinicAddress);

	List<Vet> findByExperienceGreaterThanEqual(int experience);

	Optional<Vet> findByUsername(String username);

	Optional<Vet> findByEmail(String email);

	boolean existsByUsername(String username);

}

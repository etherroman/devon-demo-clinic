package dev.devon.clinic.repository;

import dev.devon.clinic.domain.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface PetRepository extends JpaRepository<Pet, Long> {
    List<Pet> findByOwnerIdOrderByIdAsc(Long ownerId);
    Optional<Pet> findByIdAndOwnerId(Long id, Long ownerId);
    Optional<Pet> findFirstByOwnerIdAndNameOrderByIdAsc(Long ownerId, String name);
    boolean existsByOwnerId(Long ownerId);
}

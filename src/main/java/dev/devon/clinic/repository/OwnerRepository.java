package dev.devon.clinic.repository;

import dev.devon.clinic.domain.Owner;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
    Page<Owner> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Optional<Owner> findFirstByNameAndTelephoneOrderByIdAsc(String name, String telephone);
}

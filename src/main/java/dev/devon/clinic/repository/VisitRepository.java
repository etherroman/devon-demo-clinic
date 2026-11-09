package dev.devon.clinic.repository;

import dev.devon.clinic.domain.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface VisitRepository extends JpaRepository<Visit, Long> {
    List<Visit> findByPetIdOrderByVisitDateDescIdDesc(Long petId);
    boolean existsByPetId(Long petId);
    boolean existsByPetIdAndVisitDateBefore(Long petId, LocalDate date);
    boolean existsByPetIdAndVisitDateAndDescription(Long petId, LocalDate date, String description);
}

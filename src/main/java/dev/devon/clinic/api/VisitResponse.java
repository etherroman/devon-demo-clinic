package dev.devon.clinic.api;

import dev.devon.clinic.domain.Visit;
import java.time.LocalDate;

public record VisitResponse(Long id, Long petId, LocalDate visitDate, String description) {
    public static VisitResponse from(Visit visit) {
        return new VisitResponse(visit.getId(), visit.getPet().getId(), visit.getVisitDate(), visit.getDescription());
    }
}

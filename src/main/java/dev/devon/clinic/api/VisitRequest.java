package dev.devon.clinic.api;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record VisitRequest(
    @NotNull @PastOrPresent LocalDate visitDate,
    @NotBlank @Size(max = 1000) String description
) {}

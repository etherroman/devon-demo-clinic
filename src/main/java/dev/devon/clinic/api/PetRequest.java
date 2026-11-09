package dev.devon.clinic.api;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record PetRequest(
    @NotBlank @Size(max = 100) String name,
    @NotBlank @Size(max = 50) String type,
    @NotNull @PastOrPresent LocalDate birthDate
) {}

package dev.devon.clinic.api;

import jakarta.validation.constraints.*;

public record OwnerRequest(
    @NotBlank @Size(max = 100) String name,
    @NotBlank @Size(max = 30) String telephone,
    @NotBlank @Size(max = 255) String address
) {}

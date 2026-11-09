package dev.devon.clinic.api;

import dev.devon.clinic.domain.Pet;
import java.time.LocalDate;

public record PetResponse(Long id, Long ownerId, String name, String type, LocalDate birthDate) {
    public static PetResponse from(Pet pet) {
        return new PetResponse(pet.getId(), pet.getOwner().getId(), pet.getName(), pet.getType(), pet.getBirthDate());
    }
}

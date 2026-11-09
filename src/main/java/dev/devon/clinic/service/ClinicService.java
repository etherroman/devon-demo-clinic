package dev.devon.clinic.service;

import dev.devon.clinic.api.*;
import dev.devon.clinic.domain.*;
import dev.devon.clinic.repository.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ClinicService {
    private final OwnerRepository owners;
    private final PetRepository pets;
    private final VisitRepository visits;

    public ClinicService(OwnerRepository owners, PetRepository pets, VisitRepository visits) {
        this.owners = owners;
        this.pets = pets;
        this.visits = visits;
    }

    public PageResponse<OwnerResponse> searchOwners(String name, int page, int size) {
        return PageResponse.from(owners.findByNameContainingIgnoreCase(name.strip(),
            PageRequest.of(page, size, Sort.by("id").ascending())).map(OwnerResponse::from));
    }
    public OwnerResponse getOwner(long id) { return OwnerResponse.from(requireOwner(id)); }
    @Transactional public OwnerResponse createOwner(OwnerRequest input) {
        return OwnerResponse.from(owners.save(new Owner(input.name(), input.telephone(), input.address())));
    }
    @Transactional public OwnerResponse updateOwner(long id, OwnerRequest input) {
        Owner owner = requireOwner(id);
        owner.update(input.name(), input.telephone(), input.address());
        return OwnerResponse.from(owner);
    }
    @Transactional public void deleteOwner(long id) {
        Owner owner = requireOwner(id);
        if (pets.existsByOwnerId(id)) throw ApiException.conflict("Delete the owner's pets before deleting the owner");
        owners.delete(owner);
        owners.flush();
    }
    public List<PetResponse> listPets(long ownerId) {
        requireOwner(ownerId);
        return pets.findByOwnerIdOrderByIdAsc(ownerId).stream().map(PetResponse::from).toList();
    }
    public PetResponse getPet(long ownerId, long id) { return PetResponse.from(requirePet(ownerId, id)); }
    @Transactional public PetResponse createPet(long ownerId, PetRequest input) {
        return PetResponse.from(pets.save(new Pet(requireOwner(ownerId), input.name(), input.type(), input.birthDate())));
    }
    @Transactional public PetResponse updatePet(long ownerId, long id, PetRequest input) {
        Pet pet = requirePet(ownerId, id);
        if (visits.existsByPetIdAndVisitDateBefore(id, input.birthDate())) {
            throw ApiException.invalid("Birth date must not be after an existing visit");
        }
        pet.update(input.name(), input.type(), input.birthDate());
        return PetResponse.from(pet);
    }
    @Transactional public void deletePet(long ownerId, long id) {
        Pet pet = requirePet(ownerId, id);
        if (visits.existsByPetId(id)) throw ApiException.conflict("A pet with visits cannot be deleted");
        pets.delete(pet);
        pets.flush();
    }
    public List<VisitResponse> listVisits(long ownerId, long petId) {
        requirePet(ownerId, petId);
        return visits.findByPetIdOrderByVisitDateDescIdDesc(petId).stream().map(VisitResponse::from).toList();
    }
    @Transactional public VisitResponse createVisit(long ownerId, long petId, VisitRequest input) {
        Pet pet = requirePet(ownerId, petId);
        if (input.visitDate().isBefore(pet.getBirthDate())) throw ApiException.invalid("Visit date must be on or after birth date");
        return VisitResponse.from(visits.save(new Visit(pet, input.visitDate(), input.description())));
    }
    private Owner requireOwner(long id) { return owners.findById(id).orElseThrow(() -> ApiException.missing("Owner")); }
    private Pet requirePet(long ownerId, long id) {
        return pets.findByIdAndOwnerId(id, ownerId).orElseThrow(() -> ApiException.missing("Pet for owner"));
    }
}

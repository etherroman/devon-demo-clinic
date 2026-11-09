package dev.devon.clinic.api;

import dev.devon.clinic.service.ClinicService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/owners/{ownerId}/pets")
public class PetController {
    private final ClinicService service;
    public PetController(ClinicService service) { this.service = service; }
    @GetMapping public List<PetResponse> list(@PathVariable @Positive long ownerId) { return service.listPets(ownerId); }
    @PostMapping public ResponseEntity<PetResponse> create(@PathVariable @Positive long ownerId, @Valid @RequestBody PetRequest input) {
        PetResponse result = service.createPet(ownerId, input);
        return ResponseEntity.created(URI.create("/api/owners/" + ownerId + "/pets/" + result.id())).body(result);
    }
    @GetMapping("/{id}") public PetResponse get(@PathVariable @Positive long ownerId, @PathVariable @Positive long id) {
        return service.getPet(ownerId, id);
    }
    @PutMapping("/{id}") public PetResponse update(@PathVariable @Positive long ownerId, @PathVariable @Positive long id,
        @Valid @RequestBody PetRequest input) { return service.updatePet(ownerId, id, input); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable @Positive long ownerId, @PathVariable @Positive long id) {
        service.deletePet(ownerId, id);
        return ResponseEntity.noContent().build();
    }
}

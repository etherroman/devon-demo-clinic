package dev.devon.clinic.api;

import dev.devon.clinic.service.ClinicService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@RestController
@RequestMapping("/api/owners")
public class OwnerController {
    private final ClinicService service;
    public OwnerController(ClinicService service) { this.service = service; }
    @GetMapping public PageResponse<OwnerResponse> list(
        @RequestParam(defaultValue = "") @Size(max = 100) String name,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.searchOwners(name, page, size);
    }
    @PostMapping public ResponseEntity<OwnerResponse> create(@Valid @RequestBody OwnerRequest input) {
        OwnerResponse result = service.createOwner(input);
        return ResponseEntity.created(URI.create("/api/owners/" + result.id())).body(result);
    }
    @GetMapping("/{id}") public OwnerResponse get(@PathVariable @Positive long id) { return service.getOwner(id); }
    @PutMapping("/{id}") public OwnerResponse update(@PathVariable @Positive long id, @Valid @RequestBody OwnerRequest input) {
        return service.updateOwner(id, input);
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable @Positive long id) {
        service.deleteOwner(id);
        return ResponseEntity.noContent().build();
    }
}

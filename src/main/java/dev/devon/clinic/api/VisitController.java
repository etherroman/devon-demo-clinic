package dev.devon.clinic.api;

import dev.devon.clinic.service.ClinicService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.net.URI;

@RestController
@RequestMapping("/api/owners/{ownerId}/pets/{petId}/visits")
public class VisitController {
    private final ClinicService service;
    public VisitController(ClinicService service) { this.service = service; }
    @GetMapping public List<VisitResponse> list(@PathVariable @Positive long ownerId, @PathVariable @Positive long petId) {
        return service.listVisits(ownerId, petId);
    }
    @PostMapping public ResponseEntity<VisitResponse> create(@PathVariable @Positive long ownerId, @PathVariable @Positive long petId,
        @Valid @RequestBody VisitRequest input) {
        VisitResponse result = service.createVisit(ownerId, petId, input);
        return ResponseEntity.created(URI.create("/api/owners/" + ownerId + "/pets/" + petId + "/visits")).body(result);
    }
}

package dev.devon.clinic.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "visits")
public class Visit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false) private Pet pet;
    @Column(nullable = false) private LocalDate visitDate;
    @Column(nullable = false, length = 1000) private String description;

    protected Visit() {}
    public Visit(Pet pet, LocalDate visitDate, String description) {
        this.pet = pet;
        this.visitDate = visitDate;
        this.description = description.strip();
    }
    public Long getId() { return id; }
    public Pet getPet() { return pet; }
    public LocalDate getVisitDate() { return visitDate; }
    public String getDescription() { return description; }
}

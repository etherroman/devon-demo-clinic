package dev.devon.clinic.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "pets")
public class Pet {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false) private Owner owner;
    @Column(nullable = false, length = 100) private String name;
    @Column(nullable = false, length = 50) private String type;
    @Column(nullable = false) private LocalDate birthDate;

    protected Pet() {}
    public Pet(Owner owner, String name, String type, LocalDate birthDate) {
        this.owner = owner;
        update(name, type, birthDate);
    }
    public void update(String name, String type, LocalDate birthDate) {
        this.name = name.strip();
        this.type = type.strip();
        this.birthDate = birthDate;
    }
    public Long getId() { return id; }
    public Owner getOwner() { return owner; }
    public String getName() { return name; }
    public String getType() { return type; }
    public LocalDate getBirthDate() { return birthDate; }
}

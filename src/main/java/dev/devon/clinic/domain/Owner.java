package dev.devon.clinic.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "owners")
public class Owner {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100) private String name;
    @Column(nullable = false, length = 30) private String telephone;
    @Column(nullable = false, length = 255) private String address;

    protected Owner() {}
    public Owner(String name, String telephone, String address) { update(name, telephone, address); }
    public void update(String name, String telephone, String address) {
        this.name = name.strip();
        this.telephone = telephone.strip();
        this.address = address.strip();
    }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getTelephone() { return telephone; }
    public String getAddress() { return address; }
}

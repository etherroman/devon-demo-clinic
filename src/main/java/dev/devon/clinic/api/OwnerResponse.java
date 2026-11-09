package dev.devon.clinic.api;

import dev.devon.clinic.domain.Owner;

public record OwnerResponse(Long id, String name, String telephone, String address) {
    public static OwnerResponse from(Owner owner) {
        return new OwnerResponse(owner.getId(), owner.getName(), owner.getTelephone(), owner.getAddress());
    }
}

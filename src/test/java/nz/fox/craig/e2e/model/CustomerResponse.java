package nz.fox.craig.e2e.model;

import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String firstName,
        String lastName,
        String displayName,
        String email,
        String address,
        String status) {
}

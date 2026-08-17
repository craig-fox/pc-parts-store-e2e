package nz.fox.craig.e2e.model;

import java.util.UUID;

public record LoginResponse(
        String token,
        UUID customerId,
        String firstName,
        String preferredName) {
}

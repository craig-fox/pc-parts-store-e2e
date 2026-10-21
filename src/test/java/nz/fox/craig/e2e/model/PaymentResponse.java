package nz.fox.craig.e2e.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID orderId,
        UUID customerId,
        BigDecimal amount,
        String currency,
        String status,
        Instant createdAt,
        Instant updatedAt) {
}

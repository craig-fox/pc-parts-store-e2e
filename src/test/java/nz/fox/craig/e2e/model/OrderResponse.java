package nz.fox.craig.e2e.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID customerId,
        LocalDateTime orderDate,
        String status,
        BigDecimal subtotal,
        BigDecimal shipping,
        BigDecimal total,
        List<OrderItemResponse> items) {
}

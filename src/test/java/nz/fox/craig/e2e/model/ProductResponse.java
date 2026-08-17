package nz.fox.craig.e2e.model;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String sku,
        String name,
        String description,
        String brand,
        String category,
        BigDecimal price,
        Integer stockQuantity,
        BigDecimal weightKg,
        String imageUrl) {
}

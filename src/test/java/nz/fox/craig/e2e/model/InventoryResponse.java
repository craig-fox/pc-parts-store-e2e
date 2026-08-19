package nz.fox.craig.e2e.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryResponse(
        UUID productId,
        int quantityOnHand,
        int quantityReserved,
        int availableQuantity,
        InventoryStatus status,
        LocalDateTime lastUpdated) {}

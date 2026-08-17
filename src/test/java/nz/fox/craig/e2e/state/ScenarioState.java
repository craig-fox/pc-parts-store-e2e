package nz.fox.craig.e2e.state;

import nz.fox.craig.e2e.model.ProductResponse;

import java.util.UUID;

public class ScenarioState {

    private UUID customerId;
    private String authToken;
    private ProductResponse selectedProduct;
    private UUID orderId;

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public ProductResponse getSelectedProduct() {
        return selectedProduct;
    }

    public void setSelectedProduct(ProductResponse selectedProduct) {
        this.selectedProduct = selectedProduct;
    }
}

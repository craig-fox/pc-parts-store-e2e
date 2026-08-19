package nz.fox.craig.e2e.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import nz.fox.craig.e2e.client.ProductClient;
import nz.fox.craig.e2e.model.ProductResponse;
import nz.fox.craig.e2e.state.ScenarioState;

public class ProductSteps {

    private final ProductClient productClient;
    private final ScenarioState state;
    private HttpResponse<String> productNotFoundResponse;

    public ProductSteps(
            ProductClient productClient,
            ScenarioState state) {

        this.productClient = productClient;
        this.state = state;
    }

    private HttpResponse<String> productResponse;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private HttpResponse<String> productDetailsResponse;

    @When("I request the available products")
    public void iRequestTheAvailableProducts() throws Exception {

        productResponse = productClient.getProducts();
    }

    @Then("the products should be returned successfully")
    public void theProductsShouldBeReturnedSuccessfully() throws Exception {
    
        assertEquals(200, productResponse.statusCode());
    
        List<ProductResponse> products =
                objectMapper.readValue(
                        productResponse.body(),
                        new TypeReference<List<ProductResponse>>() {});
    
        assertNotNull(products);
        assertFalse(products.isEmpty());

        for (ProductResponse product : products) {
            assertNotNull(product.id());
            assertNotNull(product.sku());
            assertNotNull(product.name());
            assertFalse(product.name().isBlank());
            assertNotNull(product.price());
        }
    }

    @Given("a product is available")
    public void aProductIsAvailable() throws Exception {
    
        state.setSelectedProduct(
                productClient.getE2eProduct());
    }

    @When("I request the product details")
    public void iRequestTheProductDetails() throws Exception {
    
        productDetailsResponse =
                productClient.getProduct(
                        state.getSelectedProduct().id().toString());
    }

    @Then("the product details should be returned successfully")
    public void theProductDetailsShouldBeReturnedSuccessfully() throws Exception {

        assertEquals(200, productDetailsResponse.statusCode());

        ProductResponse product =
                objectMapper.readValue(
                        productDetailsResponse.body(),
                        ProductResponse.class);

        assertNotNull(product.id());
        assertEquals(state.getSelectedProduct().id(), product.id());

        assertEquals(state.getSelectedProduct().sku(), product.sku());
        assertEquals(state.getSelectedProduct().name(), product.name());
        assertEquals(state.getSelectedProduct().description(), product.description());
        assertEquals(state.getSelectedProduct().brand(), product.brand());
        assertEquals(state.getSelectedProduct().category(), product.category());
        assertEquals(state.getSelectedProduct().price(), product.price());
        assertEquals(state.getSelectedProduct().stockQuantity(), product.stockQuantity());
        assertEquals(state.getSelectedProduct().weightKg(), product.weightKg());
        assertEquals(state.getSelectedProduct().imageUrl(), product.imageUrl());
    }

    @When("I request the details of a nonexistent product")
    public void iRequestTheDetailsOfANonexistentProduct() throws Exception {

        UUID productId = UUID.randomUUID();

        productNotFoundResponse =
                productClient.getProduct(productId.toString());
    }

    @Then("the product should not be found")
    public void theProductShouldNotBeFound() {
        assertEquals(404, productNotFoundResponse.statusCode());
    }
}

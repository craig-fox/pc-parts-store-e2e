package nz.fox.craig.e2e.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import nz.fox.craig.e2e.config.E2eConfig;
import nz.fox.craig.e2e.model.ProductResponse;
import nz.fox.craig.e2e.state.ScenarioState;

public class ProductSteps {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private HttpResponse<String> productResponse;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private HttpResponse<String> productDetailsResponse;


    private final ScenarioState state;

    public ProductSteps(ScenarioState state) {
        this.state = state;
    }

    @When("I request the available products")
    public void iRequestTheAvailableProducts() throws Exception {

       

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(
                                E2eConfig.PRODUCT_SERVICE_URL
                                        + "/api/products"))
                        .GET()
                        .build();

        productResponse =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString());
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
            assertFalse(product.name().isBlank());
            assertNotNull(product.price());
        }
    }

    @Given("a product is available")
    public void aProductIsAvailable() throws Exception {

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(
                                E2eConfig.PRODUCT_SERVICE_URL
                                        + "/api/products"))
                        .GET()
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<ProductResponse> products =
                objectMapper.readValue(
                        response.body(),
                        new TypeReference<List<ProductResponse>>() {});

        assertFalse(products.isEmpty());
        state.setSelectedProduct(products.get(0));
    }

    @When("I request the product details")
    public void iRequestTheProductDetails() throws Exception {

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(
                                E2eConfig.PRODUCT_SERVICE_URL
                                        + "/api/products/"
                                        + state.getSelectedProduct().id()))
                        .GET()
                        .build();

        productDetailsResponse =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString());
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
}

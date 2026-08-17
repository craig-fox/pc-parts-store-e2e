package nz.fox.craig.e2e.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import nz.fox.craig.e2e.config.E2eConfig;
import nz.fox.craig.e2e.model.CustomerResponse;
import nz.fox.craig.e2e.model.LoginResponse;
import nz.fox.craig.e2e.model.OrderItemResponse;
import nz.fox.craig.e2e.model.OrderResponse;
import nz.fox.craig.e2e.model.ProductResponse;
import nz.fox.craig.e2e.state.ScenarioState;

public class OrderSteps {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ScenarioState state;

    private HttpResponse<String> orderResponse;
    private HttpResponse<String> orderRetrievalResponse;
    private final ObjectMapper objectMapper =
        new ObjectMapper()
                .registerModule(new JavaTimeModule());
    

    public OrderSteps(ScenarioState state) {
        this.state = state;
    }

    @Given("I am a registered and authenticated customer")
    public void iAmARegisteredAndAuthenticatedCustomer() throws Exception {

        String email =
                "e2e-order-"
                        + System.currentTimeMillis()
                        + "@example.com";

        String password = "password123";

        String registrationBody =
                """
                {
                    "firstName": "E2E",
                    "lastName": "Order",
                    "preferredName": "Test",
                    "email": "%s",
                    "address": "1 Test Street",
                    "password": "%s"
                }
                """
                .formatted(email, password);

        HttpRequest registrationRequest =
                HttpRequest.newBuilder()
                        .uri(URI.create(
                                E2eConfig.CUSTOMER_SERVICE_URL
                                        + "/api/customers"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(registrationBody))
                        .build();

        HttpResponse<String> registrationResponse =
                httpClient.send(
                        registrationRequest,
                        HttpResponse.BodyHandlers.ofString());

        assertEquals(201, registrationResponse.statusCode());

        CustomerResponse customer =
                objectMapper.readValue(
                        registrationResponse.body(),
                        CustomerResponse.class);

        state.setCustomerId(customer.id());

        String loginBody =
                """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """
                .formatted(email, password);

        HttpRequest loginRequest =
                HttpRequest.newBuilder()
                        .uri(URI.create(
                                E2eConfig.AUTH_SERVICE_URL
                                        + "/api/auth/login"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(loginBody))
                        .build();

        HttpResponse<String> loginResponse =
                httpClient.send(
                        loginRequest,
                        HttpResponse.BodyHandlers.ofString());

        assertEquals(200, loginResponse.statusCode());

        LoginResponse login =
                objectMapper.readValue(
                        loginResponse.body(),
                        LoginResponse.class);

        state.setAuthToken(login.token());
    }

    @Given("a product is available to order")
    public void aProductIsAvailableToOrder() throws Exception {
    
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
    
        ProductResponse product =
                products.stream()
                        .filter(p -> p.stockQuantity() != null)
                        .filter(p -> p.stockQuantity() > 0)
                        .findFirst()
                        .orElseThrow(
                                () -> new AssertionError(
                                        "No products with available stock"));
    
        state.setSelectedProduct(product);
    }

    @When("I place an order for the product")
    public void iPlaceAnOrderForTheProduct() throws Exception {

        String requestBody =
                """
                {
                    "items": [
                        {
                            "productId": "%s",
                            "quantity": 1
                        }
                    ],
                    "shippingAddress": {
                        "addressLine1": "1 Test Street",
                        "city": "Auckland",
                        "postcode": "1010",
                        "country": "New Zealand"
                    }
                }
                """
                .formatted(state.getSelectedProduct().id());

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(
                                E2eConfig.ORDER_SERVICE_URL + "/api/orders"))
                        .header("Content-Type", "application/json")
                        .header(
                                "Authorization",
                                "Bearer " + state.getAuthToken())
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

        orderResponse =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString());
        
    }

    @Then("the order should be created successfully")
    public void theOrderShouldBeCreatedSuccessfully() throws Exception {
    
        assertEquals(201, orderResponse.statusCode());
    
        OrderResponse order =
                objectMapper.readValue(
                        orderResponse.body(),
                        OrderResponse.class);

        state.setOrderId(order.id());
    
        assertNotNull(state.getOrderId());
    
        assertEquals(
                state.getCustomerId(),
                order.customerId());
    
        assertNotNull(order.orderDate());
    
        assertEquals(
                "PLACED",
                order.status());
    
        assertNotNull(order.subtotal());
        assertNotNull(order.shipping());
        assertNotNull(order.total());
    
        assertNotNull(order.items());
        assertEquals(1, order.items().size());
    
        OrderItemResponse item = order.items().get(0);
    
        assertEquals(
                state.getSelectedProduct().id(),
                item.productId());
    
        assertEquals(
                state.getSelectedProduct().name(),
                item.productName());
    
        assertEquals(
                1,
                item.quantity());
    
        assertEquals(
                state.getSelectedProduct().price(),
                item.unitPrice());
    
        assertNotNull(item.lineTotal());
    }

    @Given("I have placed an order")
    public void iHavePlacedAnOrder() throws Exception {

        aProductIsAvailableToOrder();
        iPlaceAnOrderForTheProduct();

        assertEquals(201, orderResponse.statusCode());

        OrderResponse order =
                objectMapper.readValue(
                        orderResponse.body(),
                        OrderResponse.class);

        assertNotNull(order.id());

        state.setOrderId(order.id());
    }

    @When("I retrieve my order")
    public void iRetrieveMyOrder() throws Exception {

        assertNotNull(state.getOrderId());
        assertNotNull(state.getAuthToken());

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(
                                E2eConfig.ORDER_SERVICE_URL
                                        + "/api/orders/"
                                        + state.getOrderId()))
                        .header(
                                "Authorization",
                                "Bearer " + state.getAuthToken())
                        .GET()
                        .build();

        orderRetrievalResponse =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString());
    }

    @Then("my order should be returned successfully")
    public void myOrderShouldBeReturnedSuccessfully() throws Exception {

        assertEquals(200, orderRetrievalResponse.statusCode());

        OrderResponse order =
                objectMapper.readValue(
                        orderRetrievalResponse.body(),
                        OrderResponse.class);

        assertNotNull(order);

        assertEquals(
                state.getOrderId(),
                order.id());

        assertEquals(
                state.getCustomerId(),
                order.customerId());

        assertNotNull(order.orderDate());

        assertEquals(
                "PLACED",
                order.status());

        assertNotNull(order.subtotal());
        assertNotNull(order.shipping());
        assertNotNull(order.total());

        assertNotNull(order.items());
        assertEquals(1, order.items().size());

        OrderItemResponse item = order.items().get(0);

        assertEquals(
                state.getSelectedProduct().id(),
                item.productId());

        assertEquals(
                state.getSelectedProduct().name(),
                item.productName());

        assertEquals(
                1,
                item.quantity());

        assertEquals(
                state.getSelectedProduct().price(),
                item.unitPrice());

        assertNotNull(item.lineTotal());
    }


}

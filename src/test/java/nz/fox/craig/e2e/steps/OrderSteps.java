package nz.fox.craig.e2e.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import nz.fox.craig.e2e.client.AuthenticationClient;
import nz.fox.craig.e2e.client.InventoryClient;
import nz.fox.craig.e2e.client.OrderClient;
import nz.fox.craig.e2e.client.PaymentClient;
import nz.fox.craig.e2e.client.ProductClient;
import nz.fox.craig.e2e.model.CustomerResponse;
import nz.fox.craig.e2e.model.InventoryResponse;
import nz.fox.craig.e2e.model.LoginResponse;
import nz.fox.craig.e2e.model.OrderItemResponse;
import nz.fox.craig.e2e.model.OrderResponse;
import nz.fox.craig.e2e.model.PaymentResponse;
import nz.fox.craig.e2e.state.ScenarioState;

public class OrderSteps {

    private final OrderClient orderClient;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;
    private final ScenarioState state;
    private String idempotencyKey;
    private final AuthenticationClient authenticationClient;
    private String otherCustomerAuthToken;
    private UUID otherCustomerOrderId;
    private final PaymentClient paymentClient;
    private BigDecimal expectedOrderTotal;

    private HttpResponse<String> paymentsResponse;
    private HttpResponse<String> conflictingOrderResponse;
    private HttpResponse<String> repeatedOrderResponse;
    private HttpResponse<String> orderResponse;
    private HttpResponse<String> orderRetrievalResponse;
    private HttpResponse<String> orderCancellationResponse;
    private HttpResponse<String> orderNotFoundResponse;
    private HttpResponse<String> orderCancellationNotFoundResponse;
    private HttpResponse<String> insufficientInventoryResponse;

    private final ObjectMapper objectMapper =
        new ObjectMapper()
                .registerModule(new JavaTimeModule());

              
    
    public OrderSteps(
        OrderClient orderClient,
        ProductClient productClient,
        InventoryClient inventoryClient,
        AuthenticationClient authenticationClient,
        PaymentClient paymentClient,
        ScenarioState state) {

        this.orderClient = orderClient;
        this.productClient = productClient;
        this.inventoryClient = inventoryClient;
        this.authenticationClient = authenticationClient;
        this.paymentClient = paymentClient;
        this.state = state;
    }

    @When("I place an order for the product")
    public void iPlaceAnOrderForTheProduct() throws Exception {
        orderResponse = createOrder();
    }
     

    @Then("the order should be created successfully")
    public void theOrderShouldBeCreatedSuccessfully() throws Exception {

        assertEquals(201, orderResponse.statusCode());

        OrderResponse order =
                objectMapper.readValue(
                        orderResponse.body(),
                        OrderResponse.class);

        state.setOrderId(order.id());
        expectedOrderTotal = order.total();

        assertNotNull(state.getOrderId());

        assertEquals("PAID", order.status());

        assertOrderDetails(order);
    }


    @Given("I have placed an order")
    public void iHavePlacedAnOrder() throws Exception {

        state.setSelectedProduct(
                productClient.getE2eProduct());

        orderResponse = createOrder();

        assertEquals(
                201,
                orderResponse.statusCode(),
                orderResponse.body());

        OrderResponse order =
                objectMapper.readValue(
                        orderResponse.body(),
                        OrderResponse.class);

        assertNotNull(order.id());

        state.setOrderId(order.id());
    }

    @Given("another customer has placed an order")
    public void anotherCustomerHasPlacedAnOrder() throws Exception {
        String email = "e2e-other-customer-" + UUID.randomUUID()
                + "@example.com";
        String password = "password123";

        CustomerResponse customer =
                authenticationClient.registerCustomer(
                        "E2E",
                        "Other",
                        "Customer",
                        email,
                        "2 Test Street",
                        password);

        assertNotNull(customer.id());

        LoginResponse login =
                authenticationClient.loginCustomer(email, password);

        assertNotNull(login.token());
        assertFalse(login.token().isBlank());

        otherCustomerAuthToken = login.token();

        state.setSelectedProduct(productClient.getE2eProduct());

        HttpResponse<String> response = orderClient.createOrder(
                otherCustomerAuthToken,
                UUID.randomUUID().toString(),
                state.getSelectedProduct().id().toString(),
                1,
                "2 Test Street",
                "Auckland",
                "1010",
                "New Zealand",
                "STANDARD");

        assertEquals(201, response.statusCode(), response.body());

        OrderResponse order =
                objectMapper.readValue(response.body(), OrderResponse.class);

        otherCustomerOrderId = order.id();
        assertNotNull(otherCustomerOrderId);
    }

    @When("I retrieve my order")
    public void iRetrieveMyOrder() throws Exception {

        orderRetrievalResponse =
                orderClient.getOrder(
                        state.getAuthToken(),
                        state.getOrderId().toString());
    }

    @When("I retrieve the other customer's order")
    public void iRetrieveTheOtherCustomersOrder() throws Exception {
        orderNotFoundResponse = orderClient.getOrder(
                state.getAuthToken(),
                otherCustomerOrderId.toString());
    }

    @When("I repeat the order request with the same idempotency key")
    public void iRepeatTheOrderRequestWithTheSameIdempotencyKey()
                throws Exception {
        repeatedOrderResponse = orderClient.createOrder(
                state.getAuthToken(),
                idempotencyKey,
                state.getSelectedProduct().id().toString(),
                1,
                "1 Test Street",
                "Auckland",
                "1010",
                "New Zealand",
                "STANDARD");
    }

    @Then("my order should be returned successfully")
    public void myOrderShouldBeReturnedSuccessfully() throws Exception {

        assertEquals(200, orderRetrievalResponse.statusCode());

        OrderResponse order =
                objectMapper.readValue(
                        orderRetrievalResponse.body(),
                        OrderResponse.class);

        assertEquals("PAID", order.status());

        assertOrderDetails(order);
    }

    @When("I cancel my order")
    public void iCancelMyOrder() throws Exception {

        orderCancellationResponse =
                orderClient.cancelOrder(
                        state.getAuthToken(),
                        state.getOrderId().toString());
    }

    @Then("my order should be cancelled successfully")
    public void myOrderShouldBeCancelledSuccessfully() throws Exception {

        assertEquals(200, orderCancellationResponse.statusCode());

        OrderResponse order =
                objectMapper.readValue(
                        orderRetrievalResponse.body(),
                        OrderResponse.class);

        assertEquals("CANCELLED", order.status());

        assertOrderDetails(order);
    }

    @When("I retrieve a nonexistent order")
    public void iRetrieveANonexistentOrder() throws Exception {

        UUID orderId = UUID.randomUUID();
        orderNotFoundResponse =
                orderClient.getOrder(
                        state.getAuthToken(),
                        orderId.toString());
    }

    @Then("my order should not be found")
    public void myOrderShouldNotBeFound() {
        assertEquals(404, orderNotFoundResponse.statusCode());
    }

    @When("I cancel a nonexistent order")
    public void iCancelANonexistentOrder() throws Exception {

        UUID orderId = UUID.randomUUID();

        orderCancellationNotFoundResponse =
                orderClient.cancelOrder(
                        state.getAuthToken(),
                        orderId.toString());
    }

    @Then("the order cancellation should be rejected because the order was not found")
    public void theOrderCancellationShouldBeRejectedBecauseTheOrderWasNotFound() {
        assertEquals(404, orderCancellationNotFoundResponse.statusCode());
    }

    @When("I place an order for more stock than is available")
    public void iPlaceAnOrderForMoreStockThanIsAvailable() throws Exception {

        InventoryResponse inventory =
                inventoryClient.getInventory(state.getAuthToken(),
                        state.getSelectedProduct().id().toString());

        int quantity = inventory.availableQuantity() + 1;

        insufficientInventoryResponse =
                orderClient.createOrder(
                        state.getAuthToken(),
                        UUID.randomUUID().toString(),
                        state.getSelectedProduct().id().toString(),
                        quantity,
                        "1 Test Street",
                        "Auckland",
                        "1010",
                        "New Zealand",
                "STANDARD");
    }

    @Then("the order should be rejected because of insufficient inventory")
    public void theOrderShouldBeRejectedBecauseOfInsufficientInventory() {

        assertEquals(
                409,
                insufficientInventoryResponse.statusCode());
    }

    @Then("the existing order should be returned")
    public void theExistingOrderShouldBeReturned() throws Exception {
        assertEquals(200, repeatedOrderResponse.statusCode());

        OrderResponse originalOrder =
                objectMapper.readValue(orderResponse.body(), OrderResponse.class);

        OrderResponse repeatedOrder =
                objectMapper.readValue(
                        repeatedOrderResponse.body(), OrderResponse.class);

        assertEquals(originalOrder.id(), repeatedOrder.id());
        assertEquals(originalOrder.customerId(), repeatedOrder.customerId());
        assertEquals(originalOrder.status(), repeatedOrder.status());
    }

    @When("I repeat the order request with the same idempotency key but a different quantity")
    public void iRepeatTheOrderRequestWithSameKeyButDifferentQuantity()
                throws Exception {
        conflictingOrderResponse = orderClient.createOrder(
                state.getAuthToken(),
                idempotencyKey,
                state.getSelectedProduct().id().toString(),
                2,
                "1 Test Street",
                "Auckland",
                "1010",
                "New Zealand",
                "STANDARD");
    }

    @Then("the order request should be rejected because the idempotency key was reused")
    public void theOrderRequestShouldBeRejectedBecauseTheIdempotencyKeyWasReused() {
        assertEquals(409, conflictingOrderResponse.statusCode());
        assertEquals(
                true,
                conflictingOrderResponse.body().contains(
                        "Idempotency key has already been used with a different request"));
    }

    @When("I cancel the other customer's order")
    public void iCancelTheOtherCustomersOrder() throws Exception {
        orderCancellationNotFoundResponse = orderClient.cancelOrder(
                state.getAuthToken(),
                otherCustomerOrderId.toString());
    }

    @Then("a completed payment should exist for the order")
    public void aCompletedPaymentShouldExistForTheOrder() throws Exception {
        paymentsResponse = paymentClient.getPaymentsByCustomer(
                state.getAuthToken(),
                state.getCustomerId().toString());

        assertEquals(200, paymentsResponse.statusCode(), paymentsResponse.body());

        PaymentResponse[] payments =
                objectMapper.readValue(paymentsResponse.body(), PaymentResponse[].class);

        assertTrue(
                java.util.Arrays.stream(payments)
                        .anyMatch(payment ->
                                payment.orderId().equals(state.getOrderId())
                                        && payment.customerId().equals(state.getCustomerId())
                                        && payment.amount().compareTo(expectedOrderTotal) == 0
                                        && "NZD".equals(payment.currency())
                                        && "COMPLETED".equals(payment.status())),
                "Expected a completed NZD payment matching the order");
    }


    private HttpResponse<String> createOrder() throws Exception {
        idempotencyKey = UUID.randomUUID().toString();
    
        return orderClient.createOrder(
                state.getAuthToken(),
                idempotencyKey,
                state.getSelectedProduct().id().toString(),
                1,
                "1 Test Street",
                "Auckland",
                "1010",
                "New Zealand",
                "STANDARD");
    }

    @When("the other customer retrieves their order")
    public void theOtherCustomerRetrievesTheirOrder() throws Exception {
        orderRetrievalResponse = orderClient.getOrder(
                otherCustomerAuthToken,
                otherCustomerOrderId.toString());
    }

    @Then("the other customer's order should remain paid")
    public void theOtherCustomersOrderShouldRemainPaid() throws Exception {
        assertEquals(200, orderRetrievalResponse.statusCode());

        OrderResponse order = objectMapper.readValue(
                orderRetrievalResponse.body(),
                OrderResponse.class);

        assertEquals(otherCustomerOrderId, order.id());
        assertEquals("PAID", order.status());
    }

    private void assertOrderDetails(OrderResponse order) {

        assertNotNull(order);
        assertEquals(state.getOrderId(), order.id());
        assertEquals(state.getCustomerId(), order.customerId());
    
        assertNotNull(order.orderDate());
        assertNotNull(order.subtotal());
        assertNotNull(order.shipping());
        assertNotNull(order.total());
    
        assertNotNull(order.items());
        assertEquals(1, order.items().size());
    
        OrderItemResponse item = order.items().getFirst();
    
        assertEquals(
                state.getSelectedProduct().id(),
                item.productId());
    
        assertEquals(
                state.getSelectedProduct().name(),
                item.productName());
    
        assertEquals(1, item.quantity());
    
        assertEquals(
                state.getSelectedProduct().price(),
                item.unitPrice());
    
        assertNotNull(item.lineTotal());
    }


}

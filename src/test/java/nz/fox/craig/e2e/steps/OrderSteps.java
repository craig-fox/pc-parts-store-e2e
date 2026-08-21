package nz.fox.craig.e2e.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.net.http.HttpResponse;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import nz.fox.craig.e2e.client.InventoryClient;
import nz.fox.craig.e2e.client.OrderClient;
import nz.fox.craig.e2e.client.ProductClient;
import nz.fox.craig.e2e.model.InventoryResponse;
import nz.fox.craig.e2e.model.OrderItemResponse;
import nz.fox.craig.e2e.model.OrderResponse;
import nz.fox.craig.e2e.state.ScenarioState;

public class OrderSteps {

    private final OrderClient orderClient;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;
    private final ScenarioState state;
    

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
        ScenarioState state) {

        this.orderClient = orderClient;
        this.productClient = productClient;
        this.inventoryClient = inventoryClient;
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

        assertNotNull(state.getOrderId());

        assertEquals("PLACED", order.status());

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

    @When("I retrieve my order")
    public void iRetrieveMyOrder() throws Exception {

        orderRetrievalResponse =
                orderClient.getOrder(
                        state.getAuthToken(),
                        state.getOrderId().toString());
    }

    @Then("my order should be returned successfully")
    public void myOrderShouldBeReturnedSuccessfully() throws Exception {

        assertEquals(200, orderRetrievalResponse.statusCode());

        OrderResponse order =
                objectMapper.readValue(
                        orderRetrievalResponse.body(),
                        OrderResponse.class);

        assertEquals("PLACED", order.status());

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
                        orderCancellationResponse.body(),
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
                        "New Zealand");
    }

    @Then("the order should be rejected because of insufficient inventory")
    public void theOrderShouldBeRejectedBecauseOfInsufficientInventory() {

        assertEquals(
                409,
                insufficientInventoryResponse.statusCode());
    }

    private HttpResponse<String> createOrder() throws Exception {

        return orderClient.createOrder(
                state.getAuthToken(),
                UUID.randomUUID().toString(),
                state.getSelectedProduct().id().toString(),
                1,
                "1 Test Street",
                "Auckland",
                "1010",
                "New Zealand");
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

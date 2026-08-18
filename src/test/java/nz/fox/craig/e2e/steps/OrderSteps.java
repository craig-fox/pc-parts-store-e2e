package nz.fox.craig.e2e.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.net.http.HttpResponse;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import nz.fox.craig.e2e.client.OrderClient;
import nz.fox.craig.e2e.client.ProductClient;
import nz.fox.craig.e2e.model.OrderItemResponse;
import nz.fox.craig.e2e.model.OrderResponse;
import nz.fox.craig.e2e.model.ProductResponse;
import nz.fox.craig.e2e.state.ScenarioState;

public class OrderSteps {

    private final OrderClient orderClient;
    private final ProductClient productClient;
    private final ScenarioState state;
    

    private HttpResponse<String> orderResponse;
    private HttpResponse<String> orderRetrievalResponse;
    private HttpResponse<String> orderCancellationResponse;

    private final ObjectMapper objectMapper =
        new ObjectMapper()
                .registerModule(new JavaTimeModule());
    
    public OrderSteps(
        OrderClient orderClient,
        ProductClient productClient,
        ScenarioState state) {

        this.orderClient = orderClient;
        this.productClient = productClient;
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

        state.setSelectedProduct(
                productClient.getAvailableProduct());

        orderResponse = createOrder();

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

    @When("I cancel my order")
    public void iCancelMyOrder() throws Exception {

        orderCancellationResponse =
                orderClient.cancelOrder(
                        state.getAuthToken(),
                        state.getOrderId().toString());
    }

    @Then("my order should be cancelled successfully")
    public void myOrderShouldBeCancelledSuccessfully() throws Exception {

        assertEquals(
                200,
                orderCancellationResponse.statusCode());

        OrderResponse order =
                objectMapper.readValue(
                        orderCancellationResponse.body(),
                        OrderResponse.class);

        assertNotNull(order);

        assertEquals(
                state.getOrderId(),
                order.id());

        assertEquals(
                state.getCustomerId(),
                order.customerId());

        assertEquals(
                "CANCELLED",
                order.status());

        assertNotNull(order.orderDate());

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
                1,
                item.quantity());
    }

    private HttpResponse<String> createOrder() throws Exception {

        return orderClient.createOrder(
                state.getAuthToken(),
                state.getSelectedProduct().id().toString(),
                1,
                "1 Test Street",
                "Auckland",
                "1010",
                "New Zealand");
    }


}

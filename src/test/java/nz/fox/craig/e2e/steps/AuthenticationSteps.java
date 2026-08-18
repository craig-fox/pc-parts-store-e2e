package nz.fox.craig.e2e.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.net.http.HttpResponse;
import java.util.UUID;

import nz.fox.craig.e2e.client.AuthenticationClient;
import nz.fox.craig.e2e.model.CustomerResponse;
import nz.fox.craig.e2e.model.LoginResponse;
import nz.fox.craig.e2e.state.ScenarioState;

public class AuthenticationSteps {

    private final AuthenticationClient authenticationClient;
    private final ScenarioState state;
    private final ObjectMapper objectMapper;

    private HttpResponse<String> registrationResponse;
    private HttpResponse<String> loginResponse;

    private String email;
    private String password;

    public AuthenticationSteps(
            AuthenticationClient authenticationClient,
            ScenarioState state) {

        this.authenticationClient = authenticationClient;
        this.state = state;
        this.objectMapper =new ObjectMapper()
                .registerModule(new JavaTimeModule());        
    }

    @Given("I am an unauthenticated customer")
    public void iAmAnUnauthenticatedCustomer() {

        email = "e2e-registration-" + UUID.randomUUID() + "@example.com";
        password = "password123";
    }

    @When("I register with valid customer details")
    public void iRegisterWithValidCustomerDetails() throws Exception {

        registrationResponse =
                authenticationClient.register(
                        "E2E",
                        "Customer",
                        "Test",
                        email,
                        "1 Test Street",
                        password);
    }

    @Then("my registration should be successful")
    public void myRegistrationShouldBeSuccessful() throws Exception {

        assertEquals(201, registrationResponse.statusCode());

        CustomerResponse customerResponse =
                objectMapper.readValue(
                        registrationResponse.body(),
                        CustomerResponse.class);

        assertNotNull(customerResponse.id());

        assertEquals(
                "E2E",
                customerResponse.firstName());

        assertEquals(
                "Customer",
                customerResponse.lastName());

        assertEquals(
                "Test",
                customerResponse.displayName());

        assertEquals(
                email,
                customerResponse.email());

        assertEquals(
                "1 Test Street",
                customerResponse.address());

        assertEquals(
                "ACTIVE",
                customerResponse.status());

        state.setCustomerId(customerResponse.id());
    }

    @Given("I am a registered customer")
    public void iAmARegisteredCustomer() throws Exception {
    
        email =
                "e2e-login-"
                        + System.currentTimeMillis()
                        + "@example.com";
    
        password = "password123";
    
        CustomerResponse customer =
                authenticationClient.registerCustomer(
                        "E2E",
                        "Login",
                        "Test",
                        email,
                        "1 Test Street",
                        password);
    
        assertNotNull(customer.id());
    
        state.setCustomerId(customer.id());
    }

    @When("I log in with valid credentials")
    public void iLogInWithValidCredentials() throws Exception {

        loginResponse =
                authenticationClient.login(
                        email,
                        password);
    }

    @Then("my login should be successful")
    public void myLoginShouldBeSuccessful() throws Exception {

        assertEquals(200, loginResponse.statusCode());

        LoginResponse response =
                objectMapper.readValue(
                        loginResponse.body(),
                        LoginResponse.class);

        assertNotNull(response.token());
        assertFalse(response.token().isBlank());

        assertEquals(
                state.getCustomerId(),
                response.customerId());

        assertEquals(
                "E2E",
                response.firstName());

        assertEquals(
                "Test",
                response.preferredName());

        state.setAuthToken(response.token());
    }

    @Given("I am a registered and authenticated customer")
    public void iAmARegisteredAndAuthenticatedCustomer() throws Exception {
    
        email =
                "e2e-order-"
                        + UUID.randomUUID()
                        + "@example.com";
    
        password = "password123";
    
        CustomerResponse customer =
                registerCustomer(email, password, "Test");
    
        LoginResponse login =
                authenticationClient.loginCustomer(
                        email,
                        password);
    
        assertNotNull(login.token());
        assertFalse(login.token().isBlank());
    
        assertEquals(
                customer.id(),
                login.customerId());
    
        state.setAuthToken(login.token());
    }

    private CustomerResponse registerCustomer(
        String email,
        String password,
        String preferredName) throws Exception {

        CustomerResponse customer =
                authenticationClient.registerCustomer(
                        "E2E",
                        "Order",
                        preferredName,
                        email,
                        "1 Test Street",
                        password);

        assertNotNull(customer.id());

        state.setCustomerId(customer.id());

        return customer;
    }

}
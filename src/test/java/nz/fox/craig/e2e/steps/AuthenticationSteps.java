package nz.fox.craig.e2e.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import nz.fox.craig.e2e.config.E2eConfig;
import nz.fox.craig.e2e.model.CustomerResponse;
import nz.fox.craig.e2e.model.LoginResponse;

public class AuthenticationSteps {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private HttpResponse<String> registrationResponse;
    private HttpResponse<String> loginResponse;

    private String email;
    private String password;
    private UUID customerId;

    @Given("I am an unauthenticated customer")
    public void iAmAnUnauthenticatedCustomer() {
        email = "e2e-registration-"
                + System.currentTimeMillis()
                + "@example.com";

        password = "password123";
    }

    @When("I register with valid customer details")
    public void iRegisterWithValidCustomerDetails() throws Exception {

        String requestBody =
                """
                {
                    "firstName": "E2E",
                    "lastName": "Customer",
                    "preferredName": "Test",
                    "email": "%s",
                    "address": "1 Test Street",
                    "password": "%s"
                }
                """
                .formatted(email, password);

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(
                                E2eConfig.CUSTOMER_SERVICE_URL
                                        + "/api/customers"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

        registrationResponse =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString());
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

        customerId = customerResponse.id();
    }

    @Given("I am a registered customer")
    public void iAmARegisteredCustomer() throws Exception {

        email = "e2e-login-"
                + System.currentTimeMillis()
                + "@example.com";

        password = "password123";

        String requestBody =
                """
                {
                    "firstName": "E2E",
                    "lastName": "Login",
                    "preferredName": "Test",
                    "email": "%s",
                    "address": "1 Test Street",
                    "password": "%s"
                }
                """
                .formatted(email, password);

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(
                                E2eConfig.CUSTOMER_SERVICE_URL
                                        + "/api/customers"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

        registrationResponse =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString());

        assertEquals(201, registrationResponse.statusCode());

        CustomerResponse customerResponse =
                objectMapper.readValue(
                        registrationResponse.body(),
                        CustomerResponse.class);

        customerId = customerResponse.id();
    }

    @When("I log in with valid credentials")
    public void iLogInWithValidCredentials() throws Exception {

        String requestBody =
                """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """
                .formatted(email, password);

        HttpRequest request =
            HttpRequest.newBuilder()
                    .uri(URI.create(
                            E2eConfig.AUTH_SERVICE_URL
                                    + "/api/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

        loginResponse =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString());
        
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
                customerId,
                response.customerId());

        assertEquals(
                "E2E",
                response.firstName());

        assertEquals(
                "Test",
                response.preferredName());
    }
}
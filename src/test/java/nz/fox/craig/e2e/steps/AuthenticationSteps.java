package nz.fox.craig.e2e.steps;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuthenticationSteps {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private HttpResponse<String> response;

    @Given("I am an unauthenticated customer")
    public void iAmAnUnauthenticatedCustomer() {
        
    }

    @When("I register with valid customer details")
    public void iRegisterWithValidCustomerDetails() throws IOException, InterruptedException {
        String requestBody = """
                {
                    "firstName": "Test",
                    "lastName": "Customer",
                    "preferredName": "Tester",
                    "email": "e2e-test@example.com",
                    "address": "123 Test Street",
                    "password": "TestPassword123"
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/api/customers"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString());
    }

    @Then("my registration should be successful")
    public void myRegistrationShouldBeSuccessful() {
        assertEquals(201, response.statusCode());
    }
}

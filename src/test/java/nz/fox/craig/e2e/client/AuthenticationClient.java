package nz.fox.craig.e2e.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import nz.fox.craig.e2e.config.E2eConfig;
import nz.fox.craig.e2e.model.CustomerResponse;
import nz.fox.craig.e2e.model.LoginResponse;

public class AuthenticationClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AuthenticationClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public HttpResponse<String> register(
            String firstName,
            String lastName,
            String preferredName,
            String email,
            String address,
            String password) throws Exception {

        String requestBody =
                """
                {
                    "firstName": "%s",
                    "lastName": "%s",
                    "preferredName": "%s",
                    "email": "%s",
                    "address": "%s",
                    "password": "%s"
                }
                """
                .formatted(
                        firstName,
                        lastName,
                        preferredName,
                        email,
                        address,
                        password);

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(
                                E2eConfig.CUSTOMER_SERVICE_URL
                                        + "/api/customers"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

        return httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString());
    }

    public CustomerResponse registerCustomer(
            String firstName,
            String lastName,
            String preferredName,
            String email,
            String address,
            String password) throws Exception {

        HttpResponse<String> response =
                register(
                        firstName,
                        lastName,
                        preferredName,
                        email,
                        address,
                        password);

        return objectMapper.readValue(
                response.body(),
                CustomerResponse.class);
    }

    public HttpResponse<String> login(
            String email,
            String password) throws Exception {

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

        return httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString());
    }

    public LoginResponse loginCustomer(
            String email,
            String password) throws Exception {

        HttpResponse<String> response =
                login(email, password);

        return objectMapper.readValue(
                response.body(),
                LoginResponse.class);
    }
}

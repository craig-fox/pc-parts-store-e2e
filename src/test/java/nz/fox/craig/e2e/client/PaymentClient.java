package nz.fox.craig.e2e.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import nz.fox.craig.e2e.config.E2eConfig;

public class PaymentClient {

    private final HttpClient httpClient;

    public PaymentClient() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public HttpResponse<String> getPaymentsByCustomer(
            String authToken,
            String customerId) throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        E2eConfig.PAYMENT_SERVICE_URL
                                + "/api/payments/customer/"
                                + customerId))
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();

        return httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString());
    }
}

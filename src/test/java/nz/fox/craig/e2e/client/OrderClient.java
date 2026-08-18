package nz.fox.craig.e2e.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import nz.fox.craig.e2e.config.E2eConfig;

public class OrderClient {

    private final HttpClient httpClient;

    public OrderClient() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public HttpResponse<String> createOrder(
            String authToken,
            String productId,
            int quantity,
            String addressLine1,
            String city,
            String postcode,
            String country) throws Exception {

        String requestBody =
                """
                {
                    "items": [
                        {
                            "productId": "%s",
                            "quantity": %d
                        }
                    ],
                    "shippingAddress": {
                        "addressLine1": "%s",
                        "city": "%s",
                        "postcode": "%s",
                        "country": "%s"
                    }
                }
                """
                .formatted(
                        productId,
                        quantity,
                        addressLine1,
                        city,
                        postcode,
                        country);

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(
                                E2eConfig.ORDER_SERVICE_URL
                                        + "/api/orders"))
                        .header("Content-Type", "application/json")
                        .header(
                                "Authorization",
                                "Bearer " + authToken)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

        return httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> getOrder(
            String authToken,
            String orderId) throws Exception {

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(
                                E2eConfig.ORDER_SERVICE_URL
                                        + "/api/orders/"
                                        + orderId))
                        .header(
                                "Authorization",
                                "Bearer " + authToken)
                        .GET()
                        .build();

        return httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> cancelOrder(
        String authToken,
        String orderId) throws Exception {

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(
                                E2eConfig.ORDER_SERVICE_URL
                                        + "/api/orders/"
                                        + orderId
                                        + "/cancel"))
                        .header(
                                "Authorization",
                                "Bearer " + authToken)
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build();

        return httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString());
    }
}

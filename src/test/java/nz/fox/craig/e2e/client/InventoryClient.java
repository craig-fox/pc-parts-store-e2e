package nz.fox.craig.e2e.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import nz.fox.craig.e2e.config.E2eConfig;
import nz.fox.craig.e2e.model.InventoryResponse;

public class InventoryClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public InventoryClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper =
                new ObjectMapper()
                        .registerModule(new JavaTimeModule());
    }

    public InventoryResponse getInventory(
        String authToken,
        String productId) throws Exception {

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        E2eConfig.INVENTORY_SERVICE_URL
                                                + "/api/inventory/"
                                                + productId))
                        .header(
                                "Authorization",
                                "Bearer " + authToken)
                        .GET()
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString());

        return objectMapper.readValue(
                response.body(),
                InventoryResponse.class);
    }
}

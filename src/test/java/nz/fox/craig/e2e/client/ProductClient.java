package nz.fox.craig.e2e.client;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import nz.fox.craig.e2e.config.E2eConfig;
import nz.fox.craig.e2e.model.ProductResponse;

public class ProductClient {

    private static final String E2E_PRODUCT_SKU = "CPU-AMD-9800X3D";

    private final HttpClient httpClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProductClient() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public HttpResponse<String> getProducts() throws Exception {

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(
                                E2eConfig.PRODUCT_SERVICE_URL
                                        + "/api/products"))
                        .GET()
                        .build();

        return httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> getProduct(String productId) throws Exception {

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(
                                E2eConfig.PRODUCT_SERVICE_URL
                                        + "/api/products/"
                                        + productId))
                        .GET()
                        .build();

        return httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString());
    }

    public List<ProductResponse> getAvailableProducts() throws Exception {

        HttpResponse<String> rawData = getProducts();
    
        List<ProductResponse> products =
                objectMapper.readValue(
                        rawData.body(),
                        new TypeReference<List<ProductResponse>>() {});
    
        return products.stream()
                .filter(product -> product.stockQuantity() != null)
                .filter(product -> product.stockQuantity() > 0)
                .toList();
    }

    public ProductResponse getE2eProduct() throws Exception {

        List<ProductResponse> products = getAvailableProducts();
    
        return products.stream()
                .filter(product -> E2E_PRODUCT_SKU.equals(product.sku()))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "E2E product '%s' is not available"
                                                .formatted(E2E_PRODUCT_SKU)));
    }
}

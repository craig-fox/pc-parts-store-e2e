package nz.fox.craig.e2e.config;

public final class E2eConfig {

    private E2eConfig() {
    }

    public static final String CUSTOMER_SERVICE_URL =
            System.getProperty(
                    "customer.service.url",
                    "http://localhost:8081");
    public static final String PRODUCT_SERVICE_URL =
            System.getProperty(
                    "product.service.url",
                    "http://localhost:8082");
        
    public static final String ORDER_SERVICE_URL =
            System.getProperty(
                    "order.service.url",
                    "http://localhost:8083");
    public static final String AUTH_SERVICE_URL =
            System.getProperty(
                    "auth.service.url",
                    "http://localhost:8085");
}

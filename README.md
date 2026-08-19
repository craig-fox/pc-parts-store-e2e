# PC Parts Store E2E Tests

End-to-end tests for the PC Parts Store application.

The project uses Cucumber with JUnit 5 to test the application through its HTTP APIs. The tests exercise the application as a whole rather than testing individual services in isolation.

## Prerequisites

The PC Parts Store API project must be cloned before running the E2E tests.

Clone the API project:

```bash
git clone https://github.com/craig-fox/pc-parts-store-api.git
```

The API project should be available alongside this project.

Docker and Docker Compose must be installed and available.

The E2E project includes a `run-e2e.sh` script that starts the application environment and runs the E2E test suite. This is the recommended way to run the complete suite.

## Running the tests

From the root of `pc-parts-store-e2e`, run:

```bash
./run-e2e.sh
```

The script starts the required application services and supporting infrastructure, waits for the services to become available, and then runs the complete Cucumber suite.

The script is the preferred entry point for running the E2E tests because it provides a consistent environment from which to execute the suite.

### Running the Maven suite directly

If the application services are already running, the Cucumber suite can also be executed directly:

```bash
mvn clean test
```

All Cucumber scenarios will be discovered and executed.

Running Maven directly does not start or stop the application services.

### Running smoke tests

A small subset of scenarios is tagged with `@smoke` to provide a fast check of the application's critical path.

Run the smoke suite with:

```bash
mvn clean test -Dcucumber.filter.tags="@smoke"
```

The smoke scenarios cover the core journey through the application:

- Customer login
- Product browsing
- Product details
- Order placement

### Running individual tagged scenarios

Negative-path scenarios have their own tags and can be run selectively. For example:

```bash
mvn clean test -Dcucumber.filter.tags="@invalid-login"
```

Other negative-path tags include:

- `@nonexistent-product`
- `@nonexistent-order-retrieval`
- `@nonexistent-order-cancellation`
- `@insufficient-inventory`

## Services

By default, the tests expect the following services to be available:

| Service | URL |
|---|---|
| Customer Service | `http://localhost:8081` |
| Order Service | `http://localhost:8082` |
| Product Service | `http://localhost:8083` |
| Inventory Service | `http://localhost:8084` |
| Authentication Service | `http://localhost:8085` |

These defaults are defined in `E2eConfig`.

Service URLs can be overridden using Maven system properties. For example:

```bash
mvn clean test   -Dcustomer.service.url=http://localhost:8081   -Dproduct.service.url=http://localhost:8083   -Dorder.service.url=http://localhost:8082   -Dinventory.service.url=http://localhost:8084   -Dauth.service.url=http://localhost:8085
```

This allows the tests to be run against different environments without changing the test source code.

## Test structure

The project separates Cucumber scenarios from HTTP communication.

```text
src/test/
├── java/nz/fox/craig/e2e/
│   ├── client/
│   │   ├── AuthenticationClient.java
│   │   ├── InventoryClient.java
│   │   ├── OrderClient.java
│   │   └── ProductClient.java
│   │
│   ├── config/
│   │   └── E2eConfig.java
│   │
│   ├── model/
│   │
│   ├── state/
│   │   └── ScenarioState.java
│   │
│   ├── steps/
│   │   ├── AuthenticationSteps.java
│   │   ├── OrderSteps.java
│   │   └── ProductSteps.java
│   │
│   └── RunCucumberTest.java
│
├── resources/
│   └── features/
│       ├── authentication/
│       ├── orders/
│       └── products/
│
└── run-e2e.sh
```

### Step definitions

Step definition classes contain the behaviour used by the Cucumber scenarios.

- `AuthenticationSteps` handles registration and login scenarios.
- `ProductSteps` handles product browsing and product details.
- `OrderSteps` handles order placement, retrieval and cancellation, including inventory-related scenarios.

### Clients

The client classes contain the HTTP communication with the application services.

- `AuthenticationClient`
- `ProductClient`
- `InventoryClient`
- `OrderClient`

This keeps HTTP request construction separate from the Cucumber step definitions.

### Scenario state

`ScenarioState` stores information that needs to be shared between steps within a scenario, such as:

- authenticated customer ID
- authentication token
- selected product
- order ID

Each scenario maintains its own state.

## Current test coverage

### Authentication

- Customer registration
- Successful customer login
- Rejected login with invalid credentials

### Products

- Browse available products
- View product details
- Reject requests for nonexistent products

### Orders

- Place an order
- Reject orders when inventory is insufficient
- Retrieve an order
- Reject requests for nonexistent orders
- Cancel an order
- Reject cancellation of nonexistent orders

The scenarios focus on the primary successful user journeys and significant negative business behaviours through the application.

## Test environment

The E2E tests run against the application services and their supporting databases.

The application environment uses Docker Compose. Each microservice uses Flyway to initialise and seed its database.

The E2E test project does not maintain a separate test database. It runs against the application environment configured for E2E testing.

The `run-e2e.sh` script is the preferred way to run the complete suite because it starts the required environment, waits for the services to become available, and then executes the tests.

## Test output

The project uses the Cucumber JUnit Platform engine with the `pretty` plugin, so running:

```bash
mvn clean test
```

produces readable Cucumber output showing each scenario and its individual steps.

## Troubleshooting

### Tests return connection errors

Use the E2E script from the root of this project:

```bash
./run-e2e.sh
```

If running Maven directly, make sure all required application services are already running and available on their configured ports.

Health endpoints are available for the application services and can be used to check whether a service is ready. For example:

```bash
curl http://localhost:8081/actuator/health
```

Check the service URLs listed above if a connection fails.

### Tests fail with authentication errors

Make sure the authentication service is running and that the customer and authentication services are both available.

### Tests fail because a product is unavailable

The order scenarios use a designated E2E product. Ensure the product database has been seeded and that the E2E product has available inventory.

The insufficient-inventory scenario queries the current inventory and requests more units than are available, so it does not intentionally consume inventory.

If individual scenarios are run repeatedly against a persistent environment, successful order scenarios can change inventory state. Running the complete E2E script provides the intended controlled test environment.

### The smoke suite passes but the full suite fails

Run the complete suite using:

```bash
./run-e2e.sh
```

The smoke suite only covers the critical application path. The full suite additionally exercises negative scenarios and other order behaviours.

## Technology

- Java 21
- Maven
- Cucumber
- JUnit 5
- Java HTTP Client
- Jackson
- Docker Compose
- Spring Boot application services

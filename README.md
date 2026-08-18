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

From the root of the `pc-parts-store-api` project, start the application services with:

```bash
docker compose up -d --build
```

This starts the services and supporting infrastructure required by the E2E tests.

Once the services are running, return to this project.

## Running the tests

From the root of `pc-parts-store-e2e`, run:

```bash
mvn clean test
```

All Cucumber scenarios will be discovered and executed.

The tests expect the application services to already be running. The E2E project does not start or stop the application services itself.

## Services

By default, the tests expect the following services to be available:

| Service | URL |
|---|---|
| Customer Service | `http://localhost:8081` |
| Order Service | `http://localhost:8082` |
| Product Service | `http://localhost:8083` |
| Authentication Service | `http://localhost:8085` |

These defaults are defined in `E2eConfig`.

Service URLs can be overridden using Maven system properties. For example:

```bash
mvn clean test \
  -Dcustomer.service.url=http://localhost:8081 \
  -Dproduct.service.url=http://localhost:8083 \
  -Dorder.service.url=http://localhost:8082 \
  -Dauth.service.url=http://localhost:8085
```

This allows the tests to be run against different environments without changing the test source code.

## Test structure

The project separates Cucumber scenarios from HTTP communication.

```text
src/test/
├── java/nz/fox/craig/e2e/
│   ├── client/
│   │   ├── AuthenticationClient.java
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
└── resources/
    └── features/
        ├── authentication/
        ├── orders/
        └── products/
```

### Step definitions

Step definition classes contain the behaviour used by the Cucumber scenarios.

- `AuthenticationSteps` handles registration and login scenarios.
- `ProductSteps` handles product browsing and product details.
- `OrderSteps` handles order placement, retrieval and cancellation.

### Clients

The client classes contain the HTTP communication with the application services.

- `AuthenticationClient`
- `ProductClient`
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
- Customer login

### Products

- Browse available products
- View product details

### Orders

- Place an order
- Retrieve an order
- Cancel an order

The scenarios currently focus on the primary successful user journeys through the application.

## Test output

The project uses the Cucumber JUnit Platform engine with the `pretty` plugin, so running:

```bash
mvn clean test
```

produces readable Cucumber output showing each scenario and its individual steps.

## Troubleshooting

### Tests return connection errors

Make sure the API project is running:

```bash
cd pc-parts-store-api
docker compose up -d --build
```

You can check the running containers with:

```bash
docker compose ps
```

Application logs can be viewed with:

```bash
docker compose logs
```

or for a specific service:

```bash
docker compose logs product-service
```

### Tests fail with authentication errors

Make sure the authentication service is running and that the customer and authentication services are both available.

### Tests fail because a product is unavailable

The order scenarios select a product with available stock from the product service. Ensure the product database has been seeded and that at least one product has stock available.

## Technology

- Java
- Maven
- Cucumber
- JUnit 5
- Java HTTP Client
- Jackson
- Docker Compose

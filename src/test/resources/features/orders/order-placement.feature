Feature: Order placement

  @smoke
  Scenario: An authenticated customer can place an order
    Given I am a registered and authenticated customer
    And a product is available
    When I place an order for the product
    Then the order should be created successfully
    And a completed payment should exist for the order

  @insufficient-inventory
  Scenario: An order cannot be placed when inventory is insufficient
    Given I am a registered and authenticated customer
    And a product is available
    When I place an order for more stock than is available
    Then the order should be rejected because of insufficient inventory

  @idempotency
  Scenario: Repeating an order request returns the existing order
    Given I am a registered and authenticated customer
    And a product is available
    When I place an order for the product
    And I repeat the order request with the same idempotency key
    Then the existing order should be returned

  @idempotency
  Scenario: Reusing an idempotency key with a different request is rejected
    Given I am a registered and authenticated customer
    And a product is available
    When I place an order for the product
    And I repeat the order request with the same idempotency key but a different quantity
    Then the order request should be rejected because the idempotency key was reused

    
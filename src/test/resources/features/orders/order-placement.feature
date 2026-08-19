Feature: Order placement

  @smoke
  Scenario: An authenticated customer can place an order
    Given I am a registered and authenticated customer
    And a product is available
    When I place an order for the product
    Then the order should be created successfully

  @insufficient-inventory
  Scenario: An order cannot be placed when inventory is insufficient
    Given I am a registered and authenticated customer
    And a product is available
    When I place an order for more stock than is available
    Then the order should be rejected because of insufficient inventory

    
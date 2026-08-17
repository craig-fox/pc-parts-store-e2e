Feature: Order placement

  Scenario: An authenticated customer can place an order
    Given I am a registered and authenticated customer
    And a product is available to order
    When I place an order for the product
    Then the order should be created successfully
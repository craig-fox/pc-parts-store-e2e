Feature: Order cancellation

  Scenario: A customer can cancel their order
    Given I am a registered and authenticated customer
    And I have placed an order
    When I cancel my order
    Then my order should be cancelled successfully

  @nonexistent-order-cancellation
  Scenario: A customer cannot cancel an order that does not exist
    Given I am a registered and authenticated customer
    When I cancel a nonexistent order
    Then the order cancellation should be rejected because the order was not found
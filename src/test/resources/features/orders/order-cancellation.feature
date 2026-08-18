Feature: Order cancellation

  Scenario: A customer can cancel their order
    Given I am a registered and authenticated customer
    And I have placed an order
    When I cancel my order
    Then my order should be cancelled successfully
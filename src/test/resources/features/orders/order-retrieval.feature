Feature: Order retrieval

  Scenario: A customer can retrieve their order
    Given I am a registered and authenticated customer
    And I have placed an order
    When I retrieve my order
    Then my order should be returned successfully

  @nonexistent-order-retrieval
  Scenario: A customer cannot retrieve an order that does not exist
    Given I am a registered and authenticated customer
    When I retrieve a nonexistent order
    Then my order should not be found
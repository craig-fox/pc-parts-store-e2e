Feature: Order retrieval

  Scenario: A customer can retrieve their order
    Given I am a registered and authenticated customer
    And I have placed an order
    When I retrieve my order
    Then my order should be returned successfully
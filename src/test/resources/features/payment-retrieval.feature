Feature: Payment retrieval

  Scenario: A customer cannot retrieve another customer's payments
    Given I am a registered and authenticated customer
    And another customer has completed a payment
    When I retrieve the other customer's payments
    Then payment retrieval should be rejected
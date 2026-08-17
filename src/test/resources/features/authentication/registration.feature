Feature: Customer registration

  Scenario: A customer can register successfully
    Given I am an unauthenticated customer
    When I register with valid customer details
    Then my registration should be successful
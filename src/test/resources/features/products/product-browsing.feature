Feature: Product browsing

  Scenario: A customer can browse available products
    When I request the available products
    Then the products should be returned successfully
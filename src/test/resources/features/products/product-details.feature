Feature: Product details

  Scenario: A customer can view a product
    Given a product is available
    When I request the product details
    Then the product details should be returned successfully
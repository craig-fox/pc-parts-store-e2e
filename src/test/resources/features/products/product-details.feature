Feature: Product details

  @smoke
  Scenario: A customer can view a product
    Given a product is available
    When I request the product details
    Then the product details should be returned successfully

  @nonexistent-product
  Scenario: A customer cannot view a product that does not exist
    When I request the details of a nonexistent product
    Then the product should not be found
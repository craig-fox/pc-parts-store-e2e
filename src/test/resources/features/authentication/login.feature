Feature: Customer login

  @smoke
  Scenario: A registered customer can log in successfully
    Given I am a registered customer
    When I log in with valid credentials
    Then my login should be successful

  @invalid-login
  Scenario: A customer cannot log in with invalid credentials
    Given I am a registered customer
    When I log in with invalid credentials
    Then my login should be rejected
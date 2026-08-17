Feature: Customer login

  Scenario: A registered customer can log in successfully
    Given I am a registered customer
    When I log in with valid credentials
    Then my login should be successful
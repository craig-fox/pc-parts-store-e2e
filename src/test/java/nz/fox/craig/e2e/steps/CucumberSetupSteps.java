package nz.fox.craig.e2e.steps;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

public class CucumberSetupSteps {

    @Given("the E2E test application is configured")
    public void e2eTestApplicationIsConfigured() {
        // Configuration will be added later.
    }

    @Then("Cucumber should execute successfully")
    public void cucumberShouldExecuteSuccessfully() {
        assertTrue(true);
    }
}

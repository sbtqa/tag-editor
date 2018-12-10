package cucumber.examples.java.calculator;

import cucumber.api.java.en.Then;

public class ShoppingStepdefs {
  @Then("^I wait for (\\.[\\d]+) seconds$")
  public void i_wait_for(int change) {}

  @Then("^I wait about (\\.[\\d]+) seconds$")
  public void i_wait_about(int change) {}
}

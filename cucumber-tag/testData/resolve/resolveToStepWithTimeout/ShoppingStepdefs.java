package cucumber.examples.java.calculator;

import cucumber.api.java.en.When;

public class ShoppingStepdefs {
  @When(timeout=100, value="^I subtract (\\d+)" + " from (\\d+)$")
  public void I_subtract_from(int arg1, int arg2) throws Throwable {
  }
}

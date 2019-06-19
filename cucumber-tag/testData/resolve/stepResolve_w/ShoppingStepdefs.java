package cucumber.examples.java.calculator;

import cucumber.api.java.en.When;

public class ShoppingStepdefs {
  @When("^способ оплаты (\\w*)$")
  public void payment_mode(String payMethod) throws Throwable {
  }
}

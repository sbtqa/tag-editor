package ru.sbtqa.tag.cucumber.inspections;

import com.intellij.codeInsight.intention.HighPriorityAction;
import org.jetbrains.annotations.NotNull;
import ru.sbtqa.tag.cucumber.CucumberBundle;
import ru.sbtqa.tag.cucumber.psi.GherkinStep;

/**
 * @author yole
 */
public class CucumberCreateStepFix extends CucumberCreateStepFixBase implements HighPriorityAction {
  @Override
  @NotNull
  public String getName() {
    return CucumberBundle.message("cucumber.create.step.title");
  }

  @Override
  protected void createStepOrSteps(GherkinStep step, @NotNull final CucumberStepDefinitionCreationContext fileAndFrameworkType) {
    createFileOrStepDefinition(step, fileAndFrameworkType);
  }
}

package ru.sbtqa.tag.cucumber.inspections;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import ru.sbtqa.tag.cucumber.CucumberBundle;
import ru.sbtqa.tag.cucumber.CucumberUtil;
import ru.sbtqa.tag.cucumber.psi.GherkinFeature;
import ru.sbtqa.tag.cucumber.psi.GherkinFile;
import ru.sbtqa.tag.cucumber.psi.GherkinStep;
import ru.sbtqa.tag.cucumber.psi.GherkinStepsHolder;
import ru.sbtqa.tag.cucumber.steps.AbstractStepDefinition;
import ru.sbtqa.tag.cucumber.steps.reference.CucumberStepReference;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class CucumberCreateAllStepsFix extends CucumberCreateStepFixBase {
  @NotNull
  @Override
  public String getName() {
    return CucumberBundle.message("cucumber.create.all.steps.title");
  }

  @Override
  protected void createStepOrSteps(GherkinStep sourceStep, @NotNull final CucumberStepDefinitionCreationContext fileAndFrameworkType) {
    final PsiFile probableGherkinFile = sourceStep.getContainingFile();
    if (!(probableGherkinFile instanceof GherkinFile)) {
      return;
    }

    final Set<String> createdStepDefPatterns = new HashSet<>();
    final GherkinFile gherkinFile = (GherkinFile)probableGherkinFile;
    for (GherkinFeature feature : gherkinFile.getFeatures()) {
      for (GherkinStepsHolder stepsHolder : feature.getScenarios()) {
        for (GherkinStep step : stepsHolder.getSteps()) {
          final PsiReference[] references = step.getReferences();
          for (PsiReference reference : references) {
            if (!(reference instanceof CucumberStepReference)) continue;

            final AbstractStepDefinition definition = ((CucumberStepReference)reference).resolveToDefinition();
            if (definition == null) {
              String pattern = Pattern.quote(step.getStepName());
              pattern = StringUtil.trimEnd(StringUtil.trimStart(pattern, "\\Q"), "\\E");
              pattern = CucumberUtil.prepareStepRegexp(pattern);
              if (!createdStepDefPatterns.contains(pattern)) {
                createFileOrStepDefinition(step, fileAndFrameworkType);
                createdStepDefPatterns.add(pattern);
              }
            }
          }
        }
      }
    }
  }
}

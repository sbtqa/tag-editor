package ru.sbtqa.tag.cucumber.inspections;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import ru.sbtqa.tag.cucumber.CucumberBundle;
import ru.sbtqa.tag.cucumber.psi.GherkinElementVisitor;
import ru.sbtqa.tag.cucumber.psi.GherkinScenario;

public class GherkinMisplacedBackgroundInspection extends GherkinInspection {
  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
    return new GherkinElementVisitor() {
      @Override
      public void visitScenario(GherkinScenario scenario) {
        if (scenario.isBackground()) {

          PsiElement element = scenario.getPrevSibling();

          while (element != null) {
            if (element instanceof GherkinScenario) {
              if (!((GherkinScenario)element).isBackground()) {
                holder.registerProblem(scenario.getFirstChild(), CucumberBundle.message("inspection.gherkin.background.after.scenario.error.message"), ProblemHighlightType.ERROR);
                break;
              }
            }
            element = element.getPrevSibling();
          }
        }
      }
    };
  }

  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return CucumberBundle.message("inspection.gherkin.background.after.scenario.name");
  }

  @NotNull
  @Override
  public String getShortName() {
    return "GherkinMisplacedBackground";
  }
}

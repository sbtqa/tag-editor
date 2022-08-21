package ru.sbtqa.tag.cucumber.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.ex.UnfairLocalInspectionTool;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import ru.sbtqa.tag.cucumber.CucumberBundle;
import ru.sbtqa.tag.cucumber.psi.GherkinElementVisitor;
import ru.sbtqa.tag.cucumber.psi.GherkinStep;
import ru.sbtqa.tag.cucumber.psi.GherkinStepsHolder;
import ru.sbtqa.tag.cucumber.steps.AbstractStepDefinition;
import ru.sbtqa.tag.cucumber.steps.CucumberStepsIndex;
import ru.sbtqa.tag.cucumber.steps.reference.CucumberStepReference;

/**
 * @author yole
 */
public class CucumberStepInspection extends GherkinInspection implements UnfairLocalInspectionTool {
  @Override
  public boolean isEnabledByDefault() {
    return true;
  }

  @Override
  @Nls
  @NotNull
  public String getDisplayName() {
    return CucumberBundle.message("cucumber.inspection.undefined.step.name");
  }

  @Override
  @NotNull
  public String getShortName() {
    return "CucumberUndefinedStep";
  }

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
    return new GherkinElementVisitor() {
      @Override
      public void visitStep(GherkinStep step) {
        super.visitStep(step);

        final PsiElement parent = step.getParent();
        if (parent instanceof GherkinStepsHolder) {
          final PsiReference[] references = step.getReferences();
          if (references.length != 1 || !(references[0] instanceof CucumberStepReference)) return;

          CucumberStepReference reference = (CucumberStepReference)references[0];
          final AbstractStepDefinition definition = reference.resolveToDefinition();
          if (definition == null) {
            CucumberCreateStepFix createStepFix = null;
            CucumberCreateAllStepsFix createAllStepsFix = null;
            if (CucumberStepsIndex.getInstance(step.getProject()).getExtensionCount() > 0) {
              createStepFix = new CucumberCreateStepFix();
              createAllStepsFix = new CucumberCreateAllStepsFix();
            }
            holder.registerProblem(reference.getElement(), reference.getRangeInElement(),
                                   CucumberBundle.message("cucumber.inspection.undefined.step.msg.name") + " #loc #ref",
                                   createStepFix, createAllStepsFix);
          }
        }
      }
    };
  }
}

package ru.sbtqa.tag.cucumber.inspections;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import ru.sbtqa.tag.cucumber.CucumberBundle;
import ru.sbtqa.tag.cucumber.psi.GherkinElementVisitor;
import ru.sbtqa.tag.cucumber.psi.GherkinScenarioOutline;
import ru.sbtqa.tag.cucumber.psi.GherkinTokenTypes;
import ru.sbtqa.tag.cucumber.psi.impl.GherkinExamplesBlockImpl;

/**
 * @author yole
 */
public class CucumberMissedExamplesInspection extends GherkinInspection {
  @Override
  public boolean isEnabledByDefault() {
    return true;
  }

  @Override
  @Nls
  @NotNull
  public String getDisplayName() {
    return CucumberBundle.message("inspection.missed.example.name");
  }

  @Override
  @NotNull
  public String getShortName() {
    return "CucumberMissedExamples";
  }

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
    return new GherkinElementVisitor() {
      @Override
      public void visitScenarioOutline(GherkinScenarioOutline outline) {
        super.visitScenarioOutline(outline);

        final GherkinExamplesBlockImpl block = PsiTreeUtil.getChildOfType(outline, GherkinExamplesBlockImpl.class);
        if (block == null) {
          ASTNode[] keyword = outline.getNode().getChildren(GherkinTokenTypes.KEYWORDS);
          if (keyword.length > 0) {

            holder.registerProblem(outline, keyword[0].getTextRange().shiftRight(-outline.getTextOffset()),
                                   CucumberBundle.message("inspection.missed.example.msg.name"), new CucumberCreateExamplesSectionFix());
          }
        }
      }
    };
  }

  @NotNull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }
}
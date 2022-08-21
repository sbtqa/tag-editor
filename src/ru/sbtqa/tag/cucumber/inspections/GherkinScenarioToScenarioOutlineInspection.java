// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package ru.sbtqa.tag.cucumber.inspections;

import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NotNull;
import ru.sbtqa.tag.cucumber.CucumberBundle;
import ru.sbtqa.tag.cucumber.psi.*;
import ru.sbtqa.tag.cucumber.psi.i18n.JsonGherkinKeywordProvider;

import java.util.Collection;
import java.util.stream.Stream;

import static ru.sbtqa.tag.cucumber.psi.GherkinElementTypes.EXAMPLES_BLOCK;

public class GherkinScenarioToScenarioOutlineInspection extends GherkinInspection {
  private static final ConvertScenarioToOutlineFix CONVERT_SCENARIO_TO_OUTLINE_FIX = new ConvertScenarioToOutlineFix();

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder,
                                        boolean isOnTheFly,
                                        @NotNull LocalInspectionToolSession session) {
    return new GherkinElementVisitor() {
      @Override
      public void visitScenario(GherkinScenario scenario) {
        if (scenario instanceof GherkinScenarioOutline) {
          return;
        }

        if (Stream.of(scenario.getChildren()).anyMatch(p -> PsiUtilCore.getElementType(p) == EXAMPLES_BLOCK)) {
          holder.registerProblem(scenario, scenario.getFirstChild().getTextRangeInParent(),
                                 CucumberBundle.message("inspection.gherkin.scenario.with.examples.section.error.message"),
                                 CONVERT_SCENARIO_TO_OUTLINE_FIX);
        }
      }
    };
  }

  private static class ConvertScenarioToOutlineFix implements LocalQuickFix {
    @Override
    @NotNull
    public String getFamilyName() {
      return CucumberBundle.message("inspection.gherkin.scenario.with.examples.section.quickfix.name");
    }

    @Override
    public void applyFix(@NotNull final Project project, @NotNull ProblemDescriptor descriptor) {
      GherkinScenario scenario = (GherkinScenario)descriptor.getPsiElement();
      String language = GherkinKeywordTable.getFeatureLanguage(scenario.getContainingFile());

      GherkinKeywordTable keywordsTable = JsonGherkinKeywordProvider.getKeywordProvider().getKeywordsTable(language);
      Collection<String> scenarioKeywords = keywordsTable.getScenarioKeywords();
      String scenarioRegexp = StringUtil.join(scenarioKeywords, "|");
      String scenarioOutlineKeyword = keywordsTable.getScenarioOutlineKeyword();

      String scenarioOutlineText = scenario.getText().replaceFirst(scenarioRegexp, scenarioOutlineKeyword);

      GherkinScenarioOutline scenarioOutline =
        (GherkinScenarioOutline)GherkinElementFactory.createScenarioFromText(project, language, scenarioOutlineText);
      scenario.replace(scenarioOutline);
    }
  }
}

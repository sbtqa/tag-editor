package org.jetbrains.plugins.cucumber.inspections.suppress;

import com.intellij.codeInsight.daemon.impl.actions.AbstractBatchSuppressByNoInspectionCommentFix;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.psi.GherkinStepsHolder;

public class GherkinSuppressForScenarioCommentFix extends AbstractBatchSuppressByNoInspectionCommentFix {

    GherkinSuppressForScenarioCommentFix(@NotNull final String toolId) {
        super(toolId, false);
    }

    @NotNull
    @Override
    public String getText() {
        return CucumberBundle.message("cucumber.inspection.suppress.scenario");
    }

    @Override
    public PsiElement getContainer(PsiElement context) {
        // steps holder
        return PsiTreeUtil.getNonStrictParentOfType(context, GherkinStepsHolder.class);
    }
}

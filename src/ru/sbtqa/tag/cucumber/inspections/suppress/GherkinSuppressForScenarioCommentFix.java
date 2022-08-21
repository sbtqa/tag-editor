package ru.sbtqa.tag.cucumber.inspections.suppress;

import com.intellij.codeInsight.daemon.impl.actions.AbstractBatchSuppressByNoInspectionCommentFix;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import ru.sbtqa.tag.cucumber.CucumberBundle;
import ru.sbtqa.tag.cucumber.psi.GherkinStepsHolder;

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

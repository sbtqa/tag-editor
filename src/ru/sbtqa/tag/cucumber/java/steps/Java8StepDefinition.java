package ru.sbtqa.tag.cucumber.java.steps;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiConstantEvaluationHelper;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiExpressionList;
import com.intellij.psi.PsiMethodCallExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Java8StepDefinition extends AbstractJavaStepDefinition {

    public Java8StepDefinition(@NotNull PsiElement element) {
        super(element);
    }

    @Nullable
    @Override
    protected String getCucumberRegexFromElement(PsiElement element) {
        if (!(element instanceof PsiMethodCallExpression)) {
            return null;
        }
        PsiExpressionList argumentList = ((PsiMethodCallExpression) element).getArgumentList();
        if (argumentList.getExpressions().length <= 1) {
            return null;
        }
        PsiExpression stepExpression = argumentList.getExpressions()[0];
        final PsiConstantEvaluationHelper evaluationHelper = JavaPsiFacade.getInstance(element.getProject()).getConstantEvaluationHelper();
        final Object constantValue = evaluationHelper.computeConstantExpression(stepExpression, false);

        if (constantValue != null) {
            if (constantValue instanceof String) {
                return (String) constantValue;
            }
        }
        return null;
    }
}

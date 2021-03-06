package org.jetbrains.plugins.cucumber.java.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.ex.BaseLocalInspectionTool;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiModifier;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.java.CucumberJavaBundle;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;

public class CucumberJavaStepDefClassIsPublicInspections extends BaseLocalInspectionTool {

    @Override
    @Nls
    @NotNull
    public String getDisplayName() {
        return CucumberJavaBundle.message("cucumber.java.inspections.step.def.class.is.public.title");
    }

    @Override
    @NotNull
    public String getShortName() {
        return "CucumberJavaStepDefClassIsPublic";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new CucumberJavaStepDefClassIsPublicVisitor(holder);
    }

    static class CucumberJavaStepDefClassIsPublicVisitor extends JavaElementVisitor {

        final ProblemsHolder holder;

        CucumberJavaStepDefClassIsPublicVisitor(final ProblemsHolder holder) {
            this.holder = holder;
        }

        @Override
        public void visitClass(PsiClass aClass) {
            if (!CucumberJavaUtil.isStepDefinitionClass(aClass)) {
                return;
            }

            if (!aClass.hasModifierProperty(PsiModifier.PUBLIC)) {
                PsiElement elementToHighlight = aClass.getNameIdentifier();
                if (elementToHighlight == null) {
                    elementToHighlight = aClass;
                }
                holder.registerProblem(elementToHighlight, CucumberJavaBundle.message("cucumber.java.inspection.step.def.class.is.public.message"));
            }
        }
    }
}

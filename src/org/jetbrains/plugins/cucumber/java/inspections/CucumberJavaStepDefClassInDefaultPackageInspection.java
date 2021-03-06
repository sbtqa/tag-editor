package org.jetbrains.plugins.cucumber.java.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.ex.BaseLocalInspectionTool;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.java.CucumberJavaBundle;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;

public class CucumberJavaStepDefClassInDefaultPackageInspection extends BaseLocalInspectionTool {

    @Override
    @Nls
    @NotNull
    public String getDisplayName() {
        return CucumberJavaBundle.message("cucumber.java.inspections.step.def.class.in.default.package.title");
    }

    @Override
    @NotNull
    public String getShortName() {
        return "CucumberJavaStepDefClassInDefaultPackage";
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
            PsiFile containingFile = aClass.getContainingFile();
            if (containingFile instanceof PsiClassOwner) {
                PsiClassOwner javaFile = (PsiClassOwner) containingFile;
                final String packageName = javaFile.getPackageName();
                if (StringUtil.isEmpty(packageName)) {
                    PsiElement elementToHighlight = aClass.getNameIdentifier();
                    if (elementToHighlight != null) {
                        holder.registerProblem(elementToHighlight,
                                CucumberJavaBundle.message("cucumber.java.inspections.step.def.class.in.default.package.message")
                        );
                    }
                }
            }
        }
    }
}

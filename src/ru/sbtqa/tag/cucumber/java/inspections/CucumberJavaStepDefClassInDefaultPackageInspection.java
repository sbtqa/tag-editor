package ru.sbtqa.tag.cucumber.java.inspections;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import ru.sbtqa.tag.cucumber.java.CucumberJavaUtil;

public class CucumberJavaStepDefClassInDefaultPackageInspection extends AbstractBaseJavaLocalInspectionTool {

    @Override
    @Nls
    @NotNull
    public String getDisplayName() {
        return "Step definition class is in default package";
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
                                "Step definition class must be in named package"
                        );
                    }
                }
            }
        }
    }
}

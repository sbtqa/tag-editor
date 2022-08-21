package ru.sbtqa.tag.cucumber.java.steps.reference;

import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;

import static ru.sbtqa.tag.cucumber.java.CucumberJavaUtil.isHook;
import static ru.sbtqa.tag.cucumber.java.CucumberJavaUtil.isStepDefinition;
import static ru.sbtqa.tag.cucumber.java.CucumberJavaUtil.isStepDefinitionClass;

public class CucumberJavaImplicitUsageProvider implements ImplicitUsageProvider {

    @Override
    public boolean isImplicitUsage(PsiElement element) {
        if (element instanceof PsiClass) {
            return isStepDefinitionClass((PsiClass) element);
        } else if (element instanceof PsiMethod) {
            return isStepDefinition((PsiMethod) element) || isHook((PsiMethod) element);
        }

        return false;
    }

    @Override
    public boolean isImplicitRead(PsiElement element) {
        return false;
    }

    @Override
    public boolean isImplicitWrite(PsiElement element) {
        return false;
    }
}

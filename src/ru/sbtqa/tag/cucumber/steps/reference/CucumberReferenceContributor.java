package ru.sbtqa.tag.cucumber.steps.reference;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import org.jetbrains.annotations.NotNull;
import ru.sbtqa.tag.cucumber.psi.impl.GherkinStepImpl;

public class CucumberReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull final PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(GherkinStepImpl.class),
                new CucumberStepReferenceProvider());

    }
}

// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package ru.sbtqa.tag.cucumber.java.run;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.sbtqa.tag.cucumber.CucumberJvmExtensionPoint;
import ru.sbtqa.tag.cucumber.psi.GherkinFile;
import ru.sbtqa.tag.cucumber.psi.GherkinScenario;
import ru.sbtqa.tag.cucumber.psi.GherkinScenarioOutline;
import ru.sbtqa.tag.cucumber.psi.GherkinStepsHolder;

/**
 * @author Andrey.Vokin
 */
public class CucumberJavaFeatureRunConfigurationProducer extends CucumberJavaRunConfigurationProducer {

    @Override
    protected NullableComputable<String> getStepsGlue(@NotNull final PsiElement element) {
        final PsiFile file = element.getContainingFile();
        if (file instanceof GherkinFile) {
            return () -> {
                final Set<String> glues = getHookGlue(element);
                for (CucumberJvmExtensionPoint extension : CucumberJvmExtensionPoint.EP_NAME.getExtensionList()) {
                    glues.addAll(extension.getGlues((GherkinFile) file, glues));
                }

                return StringUtil.join(glues, " ");
            };
        }

        return null;
    }

    @Override
    protected String getConfigurationName(@NotNull ConfigurationContext context) {
        final VirtualFile featureFile = getFileToRun(context);
        assert featureFile != null;
        return "Feature: " + featureFile.getNameWithoutExtension();
    }

    @Nullable
    @Override
    protected VirtualFile getFileToRun(ConfigurationContext context) {
        final PsiElement element = context.getPsiLocation();
        final GherkinStepsHolder scenario = PsiTreeUtil.getParentOfType(element, GherkinScenario.class, GherkinScenarioOutline.class);
        if (element != null && scenario == null && element.getContainingFile() instanceof GherkinFile) {
            return element.getContainingFile().getVirtualFile();
        }

        return null;
    }
}

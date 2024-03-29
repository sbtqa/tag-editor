// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package ru.sbtqa.tag.cucumber.java.run;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.sbtqa.tag.cucumber.CucumberBundle;
import ru.sbtqa.tag.cucumber.CucumberJvmExtensionPoint;
import ru.sbtqa.tag.cucumber.psi.GherkinFile;

public class CucumberJavaAllFeaturesInFolderRunConfigurationProducer extends CucumberJavaRunConfigurationProducer {

    @Override
    protected NullableComputable<String> getStepsGlue(@NotNull final PsiElement element) {
        final Set<String> glues = getHookGlue(element);
        if (element instanceof PsiDirectory) {
            final PsiDirectory dir = (PsiDirectory) element;
            final List<CucumberJvmExtensionPoint> extensions = CucumberJvmExtensionPoint.EP_NAME.getExtensionList();
            return new NullableComputable<String>() {
                @NotNull
                @Override
                public String compute() {
                    dir.accept(new PsiElementVisitor() {
                        @Override
                        public void visitFile(final PsiFile file) {
                            if (file instanceof GherkinFile) {
                                for (CucumberJvmExtensionPoint extension : extensions) {
                                    extension.getGlues((GherkinFile) file, glues);
                                }
                            }
                        }

                        @Override
                        public void visitDirectory(PsiDirectory dir) {
                            for (PsiDirectory subDir : dir.getSubdirectories()) {
                                subDir.accept(this);
                            }

                            for (PsiFile file : dir.getFiles()) {
                                file.accept(this);
                            }
                        }
                    });

                    return StringUtil.join(glues, " ");
                }
            };
        }
        return null;
    }

    @Override
    protected String getConfigurationName(@NotNull final ConfigurationContext context) {
        final PsiElement element = context.getPsiLocation();
        return CucumberBundle.message("cucumber.run.all.features", ((PsiDirectory) element).getVirtualFile().getName());
    }

    @Nullable
    @Override
    protected VirtualFile getFileToRun(ConfigurationContext context) {
        final PsiElement element = context.getPsiLocation();
        if (element instanceof PsiDirectory) {
            return ((PsiDirectory) element).getVirtualFile();
        }
        return null;
    }
}

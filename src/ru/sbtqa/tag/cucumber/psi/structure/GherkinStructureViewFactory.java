package ru.sbtqa.tag.cucumber.psi.structure;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.sbtqa.tag.cucumber.psi.GherkinFeature;
import ru.sbtqa.tag.cucumber.psi.GherkinFile;
import ru.sbtqa.tag.cucumber.psi.GherkinStep;
import ru.sbtqa.tag.cucumber.psi.GherkinStepsHolder;

/**
 * @author yole
 */
public class GherkinStructureViewFactory implements PsiStructureViewFactory {

    @Override
    public StructureViewBuilder getStructureViewBuilder(@NotNull final PsiFile psiFile) {
        return new TreeBasedStructureViewBuilder() {
            @NotNull
            @Override
            public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
                PsiElement root = PsiTreeUtil.getChildOfType(psiFile, GherkinFeature.class);
                if (root == null) {
                    root = psiFile;
                }
                return
                        new StructureViewModelBase(psiFile, editor, new GherkinStructureViewElement(root))
                                .withSuitableClasses(GherkinFile.class, GherkinFeature.class, GherkinStepsHolder.class, GherkinStep.class);
            }
        };
    }
}
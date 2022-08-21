package org.jetbrains.plugins.cucumber.psi.formatter;

import com.intellij.formatting.FormattingContext;
import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelBuilder;
import com.intellij.psi.PsiFile;
import com.intellij.psi.formatter.DocumentBasedFormattingModel;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.tree.FileElement;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.impl.source.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author yole
 */
public class GherkinFormattingModelBuilder implements FormattingModelBuilder {

    @Override
    @NotNull
    public FormattingModel createModel(FormattingContext context) {
        final PsiFile file = context.getContainingFile();
        final FileElement fileElement = TreeUtil.getFileElement((TreeElement) Objects.requireNonNull(SourceTreeToPsiMap.psiElementToTree(context.getPsiElement())));
        final GherkinBlock rootBlock = new GherkinBlock(fileElement);

        return new DocumentBasedFormattingModel(rootBlock, file.getProject(), context.getCodeStyleSettings(), file.getFileType(), file);
    }

}

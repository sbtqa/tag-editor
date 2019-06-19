package org.jetbrains.plugins.cucumber.psi.structure;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinFeature;
import org.jetbrains.plugins.cucumber.psi.GherkinPsiElement;
import org.jetbrains.plugins.cucumber.psi.GherkinPystring;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.psi.GherkinStepsHolder;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinFeatureHeaderImpl;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinTableImpl;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinTagImpl;

/**
 * @author yole
 */
public class GherkinStructureViewElement extends PsiTreeElementBase<PsiElement> {

    protected GherkinStructureViewElement(PsiElement psiElement) {
        super(psiElement);
    }

    @Override
    @NotNull
    public Collection<StructureViewTreeElement> getChildrenBase() {
        List<StructureViewTreeElement> result = new ArrayList<>();
        for (PsiElement element : getElement().getChildren()) {
            if (element instanceof GherkinPsiElement &&
                    !(element instanceof GherkinFeatureHeaderImpl) &&
                    !(element instanceof GherkinTableImpl) &&
                    !(element instanceof GherkinTagImpl) &&
                    !(element instanceof GherkinPystring)) {
                result.add(new GherkinStructureViewElement(element));
            }
        }
        return result;
    }

    @Override
    public Icon getIcon(boolean open) {
        final PsiElement element = getElement();
        if (element instanceof GherkinFeature
                || element instanceof GherkinStepsHolder) {
            return open ? icons.CucumberIcons.Steps_group_opened : icons.CucumberIcons.Steps_group_closed;
        }
        if (element instanceof GherkinStep) {
            return icons.CucumberIcons.Cucumber;
        }
        return null;
    }


    @Override
    public String getPresentableText() {
        return ((NavigationItem) getElement()).getPresentation().getPresentableText();
    }
}

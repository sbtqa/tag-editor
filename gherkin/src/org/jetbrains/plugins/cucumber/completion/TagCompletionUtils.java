package org.jetbrains.plugins.cucumber.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinStepImpl;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import ru.sbtqa.tag.editor.idea.utils.TagContext;
import ru.sbtqa.tag.editor.idea.utils.TagProject;

import java.util.List;
import java.util.Objects;

public class TagCompletionUtils {

    private TagCompletionUtils() {

    }

    public static boolean addPageTitles(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result, @NotNull PsiFile file) {
        PsiElement element = parameters.getPosition().getContext();
        String stepName = element instanceof GherkinStepImpl ? ((GherkinStepImpl) element).getStepName() : null;
        if (element instanceof GherkinStep && ((GherkinStep) element).findDefinitions().stream().anyMatch(AbstractStepDefinition::isContextChanger)) {

            final String startWith = stepName != null && stepName.contains("\"") ? stepName.substring(0, stepName.indexOf('"') + 1) : null;
            if (startWith != null) {
                final Project project = element.getProject();
                TagProject.pages(project)
                        .filter(Objects::nonNull)
                        .map(x -> TagProject.findPageName(x))
                        .filter(Objects::nonNull)
                        .forEach(x -> result.addElement(LookupElementBuilder.create(startWith + x).withPresentableText(x)));
                result.stopHere();
            }
            return true;
        }
        return false;
    }

    public static boolean addPageActions(CompletionParameters parameters, CompletionResultSet result, PsiFile file) {
        PsiElement element = parameters.getPosition().getContext();
        String stepName = element instanceof GherkinStepImpl ? ((GherkinStepImpl) element).getStepName() : null;

        if (element instanceof GherkinStepImpl) {
            final String currentPageName = TagContext.getCurrentPageName((GherkinStepImpl) element);
            final PsiClass pageClass = TagProject.getPageByName(element.getProject(), currentPageName);
            if (pageClass == null) {
                return false;
            }
            final List<String> actionTitles = TagProject.actionTitles(pageClass);
            if (actionTitles.isEmpty()) {
                return false;
            }
            final String startWith = stepName != null && stepName.contains("(") ? stepName.substring(0, stepName.indexOf('(') + 1) : null;
            if (startWith == null) {
                return false;
            }
            actionTitles.forEach(s -> result.addElement(LookupElementBuilder.create(startWith + s).withPresentableText(s)));
            return true;
        }
        return false;
    }
}

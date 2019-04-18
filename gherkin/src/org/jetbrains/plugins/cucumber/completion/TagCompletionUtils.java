package org.jetbrains.plugins.cucumber.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinStepImpl;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import ru.sbtqa.tag.editor.idea.utils.TagContext;
import ru.sbtqa.tag.editor.idea.utils.TagProject;

// TODO сделать не-static
class TagCompletionUtils {

    private TagCompletionUtils() {}

    public enum TagCompletion {
        ELEMENTS, ACTIONS
    }

    static boolean addPageTitles(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        PsiElement element = parameters.getPosition().getContext();
        String stepName = element instanceof GherkinStepImpl ? ((GherkinStepImpl) element).getStepName() : null;

        if (element instanceof GherkinStep && ((GherkinStep) element).findDefinitions().stream().anyMatch(AbstractStepDefinition::isContextChanger)) {
            final String startWith = stepName != null && stepName.contains("\"") ? stepName.substring(0, stepName.indexOf('"') + 1) : null;

            if (startWith != null) {
                final Project project = element.getProject();
                TagProject.getPages(project)
                        .filter(Objects::nonNull)
                        .map(TagProject::findPageName)
                        .forEach(x -> result.addElement(LookupElementBuilder.create(startWith + x).withPresentableText(x)));
                result.stopHere();
            }

            return true;
        }
        return false;
    }

    static boolean addPageActions(CompletionParameters parameters, CompletionResultSet result) {
        String placeholder = "(" + CucumberCompletionContributor.INTELLIJ_IDEA_RULEZZZ;
        return addCompletions(parameters, result, placeholder, TagCompletion.ACTIONS);
    }

    static boolean addPageElements(CompletionParameters parameters, CompletionResultSet result) {
        String placeholder = "\"" + CucumberCompletionContributor.INTELLIJ_IDEA_RULEZZZ;
        return addCompletions(parameters, result, placeholder, TagCompletion.ELEMENTS);
    }

    private static boolean addCompletions(CompletionParameters parameters, CompletionResultSet result, String placeholder, TagCompletion tagCompletion) {
        PsiElement element = parameters.getPosition().getContext();
        String stepName = element instanceof GherkinStepImpl ? ((GherkinStepImpl) element).getStepName() : null;

        if (element instanceof GherkinStep) {

            String startWith = stepName != null && stepName.contains(placeholder) ? stepName.substring(0, stepName.indexOf(placeholder) + 1) : null;
            if (startWith == null) {
                return false;
            }

            PsiClass pageClass = getPageClass(element);
            if (pageClass == null) {
                return false;
            }

            List<String> completions;
            switch (tagCompletion) {
                case ACTIONS:
                    completions = TagProject.getActionTitles(pageClass);
                    break;
                case ELEMENTS:
                    completions = TagProject.getElements(pageClass);
                    break;
                default:
                    return false;
            }

            completions.forEach(completion -> result.addElement(LookupElementBuilder.create(startWith + completion).withPresentableText(completion)));
            return true;
        }

        return false;
    }

    private static PsiClass getPageClass(PsiElement element) {
        String currentPageName = TagContext.getCurrentPageName((GherkinStepImpl) element);
        return TagProject.getPageByName(element.getProject(), currentPageName);
    }
}

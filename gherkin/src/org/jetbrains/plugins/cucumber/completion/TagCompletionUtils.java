package org.jetbrains.plugins.cucumber.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinStepImpl;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import ru.sbtqa.tag.editor.idea.utils.TagProject;

class TagCompletionUtils {

    private TagCompletionUtils() {}

    public enum TagCompletion {
        ELEMENTS, ACTIONS
    }

    static boolean addPageTitles(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        PsiElement element = parameters.getPosition().getContext();
        String stepName = element instanceof GherkinStepImpl ? ((GherkinStepImpl) element).getStepName() : null;

        if (element instanceof GherkinStep && ((GherkinStep) element).findDefinitions().stream().allMatch(AbstractStepDefinition::isUiContextChanger)) {
            final String startWith = getStartWith(stepName, "\"");

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

    static boolean addEndpointTitles(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        PsiElement element = parameters.getPosition().getContext();
        String stepName = element instanceof GherkinStepImpl ? ((GherkinStepImpl) element).getStepName() : null;

        if (element instanceof GherkinStep && ((GherkinStep) element).findDefinitions().stream().allMatch(AbstractStepDefinition::isApiContextChanger)) {
            final String startWith = getStartWith(stepName, "\"");

            if (startWith != null) {
                final Project project = element.getProject();
                TagProject.getEndpoints(project)
                        .filter(Objects::nonNull)
                        .map(TagProject::findEndpointName)
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
            String startWith = getStartWith(stepName, placeholder);
            if (startWith == null) {
                return false;
            }

            TagContext tagContext = new TagContext(element, parameters.getOriginalFile());

            if (tagContext.isEmpty()) {
                return false;
            }

            List<TagCompletionElement> completions;
            switch (tagCompletion) {
                case ACTIONS:
                    completions = TagProject.getActionTitles(tagContext);
                    break;
                case ELEMENTS:
                    completions = TagProject.getElements(tagContext);
                    break;
                default:
                    return false;
            }

            int[] index = {0};
            completions.stream()
                    .sorted(Comparator.reverseOrder())
                    .forEach(completion -> {
                        LookupElement lookupElement = LookupElementBuilder
                                .create(startWith + completion.getPresentableText())
                                .withPresentableText(completion.getPresentableText())
                                .withTypeText(completion.getTypeText());
                        result.addElement(PrioritizedLookupElement.withPriority(lookupElement, index[0]++));
                    });

            return true;
        }

        return false;
    }

    private static String getStartWith(String stepName, String placeholder) {
        return stepName != null && stepName.contains(placeholder) ? stepName.substring(0, stepName.indexOf(placeholder) + 1) : null;
    }
}

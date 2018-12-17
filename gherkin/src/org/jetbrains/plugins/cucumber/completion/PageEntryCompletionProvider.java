package org.jetbrains.plugins.cucumber.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinStepImpl;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import ru.sbtqa.tag.editor.idea.utils.TagProject;

import java.util.Objects;

/**
 * Created by SBT-Tatciy-IO on 19.07.2017.
 */
public class PageEntryCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet resultSet) {
        PsiElement element = parameters.getPosition().getContext();
        String stepName = element instanceof GherkinStepImpl ? ((GherkinStepImpl) element).getStepName() : null;


        if (element instanceof GherkinStep && ((GherkinStep) element).findDefinitions().stream().anyMatch(AbstractStepDefinition::isContextChanger)) {

            final String startWith = stepName != null && stepName.contains("\"") ? stepName.substring(0, stepName.indexOf('"') + 1) : null;

            if (startWith != null) {
                final Project project = element.getProject();
                TagProject.pages(project)
                        .filter(Objects::nonNull)
                        .map(x -> TagProject.findPageName(x, project))
                        .filter(Objects::nonNull)
                        .forEach(x -> resultSet.addElement(LookupElementBuilder.createWithSmartPointer(startWith + x, element)));
                resultSet.stopHere();
            }
        }
    }

}

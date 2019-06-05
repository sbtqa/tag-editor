package org.jetbrains.plugins.cucumber.steps;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberJvmExtensionPoint;
import org.jetbrains.plugins.cucumber.completion.TagContext;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;

public abstract class AbstractCucumberExtension implements CucumberJvmExtensionPoint {
  @Override
  public List<PsiElement> resolveStep(@NotNull final PsiElement element) {
    final Module module = ModuleUtilCore.findModuleForPsiElement(element);
    if (module == null) {
      return Collections.emptyList();
    }

    final String stepVariant = getStepVariant(element);
    if (stepVariant == null) {
      return Collections.emptyList();
    }

    final List<PsiElement> result = new ArrayList<>();

    // TODO move to method
    final List<AbstractStepDefinition> stepDefinitions = loadStepsFor(element.getContainingFile(), module);
    for (final AbstractStepDefinition stepDefinition : stepDefinitions) {
      if (stepDefinition.matches(stepVariant) && stepDefinition.supportsStep(element)) {
        result.add(stepDefinition.getElement());
      }
    }

    // TODO move to method
    List<Entry> entries = loadEntriesFor(element.getContainingFile(), module);
    TagContext context = new TagContext(element, element.getContainingFile());
    for (Entry entry : entries) {
      if (context.isCurrentElementContextChanger()) {
        if (!entry.getTitle().equals("") && stepVariant.contains("\"" + entry.getTitle() + "\"")) {
          result.add(entry.getElement());
          break;
        }
      } else {
        if ((entry.getTitle().equals(context.getCurrentTitle(false)) || entry.getTitle().equals(context.getCurrentTitle(true)))) {
          result.addAll(entry.getSupportsActions(stepVariant));
          result.addAll(entry.getSupportsElements(stepVariant, context));
        }
      }
    }




    return result;
  }

  @Nullable
  protected String getStepVariant(@NotNull final PsiElement element) {
    if (element instanceof GherkinStep) {
      return ((GherkinStep)element).getSubstitutedName();
    }
    return null;
  }

  @Override
  public void flush(@NotNull final Project project) {
  }

  @Override
  public void reset(@NotNull final Project project) {
  }

  @Override
  public Object getDataObject(@NotNull Project project) {
    return null;
  }
}

package org.jetbrains.plugins.cucumber.steps;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import java.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberJvmExtensionPoint;
import org.jetbrains.plugins.cucumber.completion.TagContext;
import org.jetbrains.plugins.cucumber.psi.*;
import ru.sbtqa.tag.editor.idea.utils.StringUtils;
import ru.sbtqa.tag.editor.idea.utils.TagProjectUtils;

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
    result.addAll(getFragment(stepVariant, module));
    result.addAll(getStepDefinitions(stepVariant, element, module));
    result.addAll(getTagEntities(stepVariant, element, module));

    return result;
  }

    private List<PsiElement> getFragment(String step, Module module) {
        final List<PsiElement> result = new ArrayList<>();

        GlobalSearchScope scope = GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.moduleScope(module), GherkinFileType.INSTANCE);
        String title = TagProjectUtils.parseTitle(step);
        for (VirtualFile virtualFile : FilenameIndex.getAllFilesByExt(scope.getProject(), "feature", scope)) {
            GherkinFile gherkinFile = (GherkinFile) PsiManager.getInstance(scope.getProject()).findFile(virtualFile);
            for (GherkinFeature feature : gherkinFile.getFeatures()) {
                for (GherkinStepsHolder scenario : feature.getScenarios()) {
                    if (scenario.getScenarioName().trim().equals(title.trim())) {
                        result.add(scenario);
                    }
                }
            }
        }

        return result;
    }

  private List<PsiElement> getStepDefinitions(String step, PsiElement element, Module module) {
    final List<PsiElement> result = new ArrayList<>();
    final List<AbstractStepDefinition> stepDefinitions = loadStepsFor(element.getContainingFile(), module);

    for (final AbstractStepDefinition stepDefinition : stepDefinitions) {
      if (stepDefinition.matches(step.replaceAll("^" + StringUtils.NON_CRITICAL, "")) && stepDefinition.supportsStep(element)) {
        result.add(stepDefinition.getElement());
      }
    }
    return result;
  }

  private List<PsiElement> getTagEntities(String step, PsiElement element, Module module) {
    final List<PsiElement> result = new ArrayList<>();
    final Map<String, Entry> entries = loadEntriesFor(element.getContainingFile(), module);

    TagContext context = new TagContext(element, element.getContainingFile());

    if (!entries.isEmpty()) {
      if (context.isContextChanger()) {
        // add entry
        String title = TagProjectUtils.parseTitle(step);
        Entry entry = entries.get(title);
        if (entry != null) {
          result.add(entry.getAnnotation());
        }
      } else {
        // add actions and elements
        if (context.getUi() != null) {
          result.addAll(entries.get(context.getCurrentTitle(true)).getSupportsActions(step));
          result.addAll(entries.get(context.getCurrentTitle(true)).getSupportsElements(step));
        }
        if (context.getApi() != null) {
          result.addAll(entries.get(context.getCurrentTitle(false)).getSupportsActions(step));
          result.addAll(entries.get(context.getCurrentTitle(false)).getSupportsElements(step));
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

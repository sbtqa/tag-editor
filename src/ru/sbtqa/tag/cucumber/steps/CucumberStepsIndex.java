// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package ru.sbtqa.tag.cucumber.steps;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.sbtqa.tag.cucumber.BDDFrameworkType;
import ru.sbtqa.tag.cucumber.CucumberJvmExtensionPoint;
import ru.sbtqa.tag.cucumber.OptionalStepDefinitionExtensionPoint;
import ru.sbtqa.tag.cucumber.inspections.CucumberStepDefinitionCreationContext;
import ru.sbtqa.tag.cucumber.psi.GherkinFile;
import ru.sbtqa.tag.cucumber.psi.GherkinStep;

import java.util.*;
import java.util.regex.Pattern;
import ru.sbtqa.tag.editor.idea.utils.StringUtils;

/**
 * @author yole
 */
public class CucumberStepsIndex {
  private static final Logger LOG = Logger.getInstance(CucumberStepsIndex.class.getName());
  private static final Map<String, List<AbstractStepDefinition>> ALL_STEPS_CACHE = new HashMap<>();

  private final Map<BDDFrameworkType, CucumberJvmExtensionPoint> myExtensionMap;
  private final Map<CucumberJvmExtensionPoint, Object> myExtensionData;
  private Project myProject;

  public static CucumberStepsIndex getInstance(Project project) {

    CucumberStepsIndex result = project.getService(CucumberStepsIndex.class);
    result.myProject = project;

    return result;
  }

  public CucumberStepsIndex(final Project project) {
    myExtensionMap = new HashMap<>();
    myExtensionData = new HashMap<>();

    for (CucumberJvmExtensionPoint e : CucumberJvmExtensionPoint.EP_NAME.getExtensionList()) {
      myExtensionMap.put(e.getStepFileType(), e);
      myExtensionData.put(e, e.getDataObject(project));
    }
  }

  public Object getExtensionDataObject(CucumberJvmExtensionPoint e) {
    return myExtensionData.get(e);
  }

  /**
   * Creates a file that will contain step definitions
   *
   * @param dir                      container for created file
   * @param fileNameWithoutExtension name of the file without "." and extension
   * @param frameworkType            type of file to create
   */
  public PsiFile createStepDefinitionFile(@NotNull final PsiDirectory dir,
                                          @NotNull final String fileNameWithoutExtension,
                                          @NotNull final BDDFrameworkType frameworkType) {
    final CucumberJvmExtensionPoint ep = myExtensionMap.get(frameworkType);
    if (ep == null) {
      LOG.error(String.format("Unsupported step definition file type %s", frameworkType));
      return null;
    }

    return ep.getStepDefinitionCreator().createStepDefinitionContainer(dir, fileNameWithoutExtension);
  }

  public boolean validateNewStepDefinitionFileName(@NotNull final PsiDirectory directory,
                                                   @NotNull final String fileName,
                                                   @NotNull final BDDFrameworkType frameworkType) {
    final CucumberJvmExtensionPoint ep = myExtensionMap.get(frameworkType);
    assert ep != null;
    return ep.getStepDefinitionCreator().validateNewStepDefinitionFileName(directory.getProject(), fileName);
  }


  /**
   * Searches for step definition.
   * More info is available in {@link #findStepDefinitions(PsiFile, GherkinStep)} doc
   *
   * @param featureFile file with steps
   * @param step        step itself
   * @return definition or null if not found
   * @see #findStepDefinitions(PsiFile, GherkinStep)
   */
  @Nullable
  public AbstractStepDefinition findStepDefinition(@NotNull final PsiFile featureFile, @NotNull final GherkinStep step) {
    final Collection<AbstractStepDefinition> definitions = findStepDefinitions(featureFile, step);
    return (definitions.isEmpty() ? null : definitions.iterator().next());
  }

  /**
   * Searches for ALL step definitions, groups it by step definition class and sorts by pattern size.
   * For each step definition class it finds the largest pattern.
   *
   * @param featureFile file with steps
   * @param step        step itself
   * @return definitions
   */
  @NotNull
  public Collection<AbstractStepDefinition> findStepDefinitions(@NotNull final PsiFile featureFile, @NotNull final GherkinStep step) {
    final Module module = ModuleUtilCore.findModuleForPsiElement(featureFile);
    if (module == null) {
      return Collections.emptyList();
    }
    String substitutedName = step.getSubstitutedName();
    if (substitutedName == null) {
      return Collections.emptyList();
    }

    // Initial caching
    if (!ALL_STEPS_CACHE.containsKey(module.getName())) {
        ALL_STEPS_CACHE.put(module.getName(), loadStepsFor(featureFile, module));
    }

    // Get step definitions from cache
    Collection<AbstractStepDefinition> stepDefs = getStepDefs(ALL_STEPS_CACHE.get(module.getName()), substitutedName, step);

      // Attempt rescan if nothing returned
    if (stepDefs.isEmpty()) {
      ALL_STEPS_CACHE.put(module.getName(), loadStepsFor(featureFile, module));
      stepDefs = getStepDefs(ALL_STEPS_CACHE.get(module.getName()), substitutedName, step);
    }
    return stepDefs;
  }

  private Collection<AbstractStepDefinition> getStepDefs(List<AbstractStepDefinition> stepDefinitions, String substitutedName, GherkinStep step) {
    Map<Class<? extends AbstractStepDefinition>, AbstractStepDefinition> definitionsByClass = new HashMap<>();

    for (AbstractStepDefinition stepDefinition : stepDefinitions) {
      if (stepDefinition != null
              && stepDefinition.matches(substitutedName.replaceAll("^" + StringUtils.NON_CRITICAL, ""))
              && stepDefinition.supportsStep(step)) {
        final Pattern currentLongestPattern = getPatternByDefinition(definitionsByClass.get(stepDefinition.getClass()));
        final Pattern newPattern = getPatternByDefinition(stepDefinition);
        final int newPatternLength = ((newPattern != null) ? newPattern.pattern().length() : -1);
        if ((currentLongestPattern == null) || (currentLongestPattern.pattern().length() < newPatternLength)) {
          definitionsByClass.put(stepDefinition.getClass(), stepDefinition);
        }
      }
    }
    return definitionsByClass.values();
  }

  /**
   * Returns pattern from step definition (if exists)
   *
   * @param definition step definition
   * @return pattern or null if it does not exist
   */
  @Nullable
  private static Pattern getPatternByDefinition(@Nullable final AbstractStepDefinition definition) {
    if (definition == null) {
      return null;
    }
    return definition.getPattern();
  }

  public List<AbstractStepDefinition> getAllStepDefinitions(@NotNull final PsiFile featureFile) {
    final Module module = ModuleUtilCore.findModuleForPsiElement(featureFile);
    if (module == null) return Collections.emptyList();
    return loadStepsFor(featureFile, module);
  }

  private List<AbstractStepDefinition> loadStepsFor(@Nullable final PsiFile featureFile, @NotNull final Module module) {
    ArrayList<AbstractStepDefinition> result = new ArrayList<>();

    for (CucumberJvmExtensionPoint extension : myExtensionMap.values()) {
      result.addAll(extension.loadStepsFor(featureFile, module));
    }
    return result;
  }

  public Set<CucumberStepDefinitionCreationContext> getStepDefinitionContainers(@NotNull final GherkinFile featureFile) {
    Set<CucumberStepDefinitionCreationContext> result = new HashSet<>();
    for (CucumberJvmExtensionPoint ep : myExtensionMap.values()) {
      // Skip if framework file creation support is optional
      if ((ep instanceof OptionalStepDefinitionExtensionPoint) &&
          !((OptionalStepDefinitionExtensionPoint)ep).participateInStepDefinitionCreation(featureFile)) {
        continue;
      }
      final Collection<? extends PsiFile> psiFiles = ep.getStepDefinitionContainers(featureFile);
      final BDDFrameworkType frameworkType = ep.getStepFileType();
      for (final PsiFile psiFile : psiFiles) {
        result.add(new CucumberStepDefinitionCreationContext(psiFile, frameworkType));
      }
    }
    return result;
  }

  public void reset() {
    for (CucumberJvmExtensionPoint e : myExtensionMap.values()) {
      e.reset(myProject);
    }
  }

  public void flush() {
    for (CucumberJvmExtensionPoint e : myExtensionMap.values()) {
      e.flush(myProject);
    }
  }

  public Map<BDDFrameworkType, CucumberJvmExtensionPoint> getExtensionMap() {
    return myExtensionMap;
  }

  public int getExtensionCount() {
    return myExtensionMap.size();
  }

  private boolean isStepLikeFile(PsiElement child, PsiElement parent) {
    if (child instanceof PsiFile) {
      final PsiFile file = (PsiFile)child;
      CucumberJvmExtensionPoint ep = myExtensionMap.get(new BDDFrameworkType(file.getFileType()));
      return ep != null && ep.isStepLikeFile(file, parent);
    }

    return false;
  }

  private boolean isWritableStepLikeFile(PsiElement child, PsiElement parent) {
    if (child instanceof PsiFile) {
      final PsiFile file = (PsiFile)child;
      CucumberJvmExtensionPoint ep = myExtensionMap.get(new BDDFrameworkType(file.getFileType()));
      return ep != null && ep.isWritableStepLikeFile(file, parent);
    }

    return false;
  }
}

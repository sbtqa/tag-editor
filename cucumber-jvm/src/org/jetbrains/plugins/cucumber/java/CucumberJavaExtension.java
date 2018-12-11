package org.jetbrains.plugins.cucumber.java;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.module.Module;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.BDDFrameworkType;
import org.jetbrains.plugins.cucumber.StepDefinitionCreator;
import org.jetbrains.plugins.cucumber.java.steps.JavaStepDefinition;
import org.jetbrains.plugins.cucumber.java.steps.JavaStepDefinitionCreator;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CucumberJavaExtension extends AbstractCucumberJavaExtension {
  public static final String CUCUMBER_RUNTIME_JAVA_STEP_DEF_ANNOTATION = "cucumber.runtime.java.StepDefAnnotation";
  public static final String ZUCHINI_RUNTIME_JAVA_STEP_DEF_ANNOTATION = "org.zuchini.annotations.StepAnnotation";

  @NotNull
  @Override
  public BDDFrameworkType getStepFileType() {
    return new BDDFrameworkType(JavaFileType.INSTANCE);
  }

  @NotNull
  @Override
  public StepDefinitionCreator getStepDefinitionCreator() {
    return new JavaStepDefinitionCreator();
  }

  @Override
  public List<AbstractStepDefinition> loadStepsFor(@Nullable PsiFile featureFile, @NotNull Module module) {
    final GlobalSearchScope dependenciesScope = module.getModuleWithDependenciesAndLibrariesScope(true);

    PsiClass stepDefAnnotationClass = JavaPsiFacade.getInstance(module.getProject()).findClass(CUCUMBER_RUNTIME_JAVA_STEP_DEF_ANNOTATION,
                                                                                               dependenciesScope);
    if (stepDefAnnotationClass == null) {
      stepDefAnnotationClass = JavaPsiFacade.getInstance(module.getProject()).findClass(ZUCHINI_RUNTIME_JAVA_STEP_DEF_ANNOTATION,
                                                                                        dependenciesScope);
    }
    if (stepDefAnnotationClass == null) {
      return Collections.emptyList();
    }

    final PsiClass aClass = JavaPsiFacade.getInstance(module.getProject()).findClass("cucumber.api.CucumberOptions",
            dependenciesScope);
    final Query<PsiClass> psiClasses = AnnotatedElementsSearch.searchPsiClasses(aClass, dependenciesScope);
    final PsiElement[] glueClasses = psiClasses.findFirst().getAnnotation("cucumber.api.CucumberOptions").findAttributeValue("glue").getChildren();

    final List<String> glue = Arrays.asList(glueClasses).stream()
            .filter(psiElement -> psiElement instanceof PsiLiteral)
            .map(literal -> literal.getText().replaceAll("\"", ""))
            .collect(Collectors.toList());

    final List<AbstractStepDefinition> result = new ArrayList<>();
    final Query<PsiClass> stepDefAnnotations = AnnotatedElementsSearch.searchPsiClasses(stepDefAnnotationClass, dependenciesScope);
    for (PsiClass annotationClass : stepDefAnnotations) {
      String annotationClassName = annotationClass.getQualifiedName();
      if (annotationClass.isAnnotationType() && annotationClassName != null) {
        final Query<PsiMethod> javaStepDefinitions = AnnotatedElementsSearch.searchPsiMethods(annotationClass, dependenciesScope);
        for (PsiMethod stepDefMethod : javaStepDefinitions) {


          final String fqdn = stepDefMethod.getContainingClass().getQualifiedName();
          final boolean isInGlue = glue.stream().anyMatch(glueElement -> fqdn.startsWith(glueElement));


          if (isInGlue) {
          result.add(new JavaStepDefinition(stepDefMethod, annotationClassName));
          }
        }
      }
    }
    return result;
  }
}

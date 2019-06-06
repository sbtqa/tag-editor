package org.jetbrains.plugins.cucumber.java;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.module.Module;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.tree.JavaElementType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.intellij.util.Query;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.BDDFrameworkType;
import org.jetbrains.plugins.cucumber.StepDefinitionCreator;
import org.jetbrains.plugins.cucumber.java.steps.JavaStepDefinition;
import org.jetbrains.plugins.cucumber.java.steps.JavaStepDefinitionCreator;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.cucumber.steps.Entry;
import ru.sbtqa.tag.editor.idea.utils.TagProject;

public class CucumberJavaExtension extends AbstractCucumberJavaExtension {

    static final String CUCUMBER_OPTIONS_ANNOTATION = "cucumber.api.CucumberOptions";
    static final String CUCUMBER_RUNTIME_JAVA_STEP_DEF_ANNOTATION = "cucumber.runtime.java.StepDefAnnotation";
    static final String ZUCHINI_RUNTIME_JAVA_STEP_DEF_ANNOTATION = "org.zuchini.annotations.StepAnnotation";
    static final String METHOD_NAME_OPEN_PAGE = "openPage";
    static final String CLASS_NAME_CORE_STEP_DEFS = "CoreStepDefs";
    static final String METHOD_NAME_SEND_REQUEST = "send";
    static final String METHOD_NAME_FILL_REQUEST = "fill";
    static final String CLASS_NAME_API_STEPS_IMPL = "ApiStepDefs";

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

        final PsiClass aClass = JavaPsiFacade.getInstance(module.getProject()).findClass(CUCUMBER_OPTIONS_ANNOTATION,
                dependenciesScope);
        final Query<PsiClass> psiClasses = AnnotatedElementsSearch.searchPsiClasses(aClass, dependenciesScope);
        final PsiElement[] glueClasses = psiClasses.findFirst()
                .getAnnotation(CUCUMBER_OPTIONS_ANNOTATION).findAttributeValue("glue").getChildren();

        final List<String> glue = Arrays.asList(glueClasses).stream()
                .filter(psiElement -> psiElement.getNode().getElementType() == JavaElementType.LITERAL_EXPRESSION)
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
                        JavaStepDefinition javaStepDefinition = new JavaStepDefinition(stepDefMethod, annotationClassName);
                        boolean isApi = (stepDefMethod.getName().equals(METHOD_NAME_SEND_REQUEST) && stepDefMethod.getParameters().length > 0
                                || stepDefMethod.getName().equals(METHOD_NAME_FILL_REQUEST))
                                && stepDefMethod.getContainingClass().getName().equals(CLASS_NAME_API_STEPS_IMPL);
                        javaStepDefinition.setApiContextChanger(isApi);

                        boolean isUi = stepDefMethod.getName().equals(METHOD_NAME_OPEN_PAGE)
                                && stepDefMethod.getContainingClass().getName().equals(CLASS_NAME_CORE_STEP_DEFS);
                        javaStepDefinition.setUiContextChanger(isUi);

                        result.add(javaStepDefinition);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Map<String, Entry> loadEntriesFor(@Nullable PsiFile featureFile, @NotNull Module module) {
        Stream<PsiClass> entries = Stream.concat(TagProject.getEndpoints(module), TagProject.getPages(module));


//        Map<String, Entry> entriesz = new HashMap<>();

        return entries.filter(Objects::nonNull)
                .distinct()
                .map(Entry::new)
                .collect(Collectors.toMap(Entry::getTitle, entry -> entry));


//        return entries.filter(Objects::nonNull)
//                .distinct()
//                .map(Entry::new)
//                .collect(Collectors.toList());


    }
}

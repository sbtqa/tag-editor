package org.jetbrains.plugins.cucumber.steps;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import ru.sbtqa.tag.editor.idea.utils.TagProject;

public class Entry {

    private PsiClass clazz;
    private List<PsiAnnotation> actions;
    private List<PsiAnnotation> elements;

    public Entry(@NotNull final PsiElement element) {
        this.clazz = (PsiClass) element;
        this.actions = getActions();
        this.elements = getElements();
    }

    private List<PsiAnnotation> getActions() {
        return TagProject.getActionAnnotations(clazz).stream()
                .map(Pair::component1)
                .collect(Collectors.toList());
    }

    private List<PsiAnnotation> getElements() {
        List<PsiAnnotation> list = Arrays.stream(clazz.getAllFields())
                .filter(TagProject::isAnnotated)
                .filter(TagProject::hasTitledAnnotation)
                .filter(field -> TagProject.getAnnotation(field) != null)
                .map(TagProject::getAnnotation)
                .collect(Collectors.toList());

        // add @Validation methods of Endpoints as element
        list.addAll(getValidations());

        return list;
    }

    private List<PsiAnnotation> getValidations() {
        return Arrays.stream(clazz.getAllMethods())
                .filter(psiMethod -> psiMethod.hasAnnotation(TagProject.VALIDATION_ANNOTATION_QUALIFIED_NAME))
                .map(psiMethod -> psiMethod.getAnnotation(TagProject.VALIDATION_ANNOTATION_QUALIFIED_NAME))
                .collect(Collectors.toList());
    }

    public String getTitle() {
        return TagProject.getAnnotationTitle(getAnnotation());
    }

    public PsiAnnotation getAnnotation() {
        PsiAnnotation endpointAnnotation = clazz.getAnnotation(TagProject.ENDPOINT_ANNOTATION_QUALIFIED_NAME);
        PsiAnnotation pageAnnotation = clazz.getAnnotation(TagProject.PAGE_ENTRY_ANNOTATION_QUALIFIED_NAME);

        return endpointAnnotation != null ? endpointAnnotation : pageAnnotation;
    }

    public List<PsiElement> getSupportsActions(String stepDef) {
        return actions.stream()
                .filter(action -> stepDef.contains("(" +  TagProject.getAnnotationTitle(action) + ")"))
                .collect(Collectors.toList());
    }

    public List<PsiElement> getSupportsElements(String stepDef) {
        return elements.stream()
                .filter(element -> stepDef.contains("\"" + TagProject.getAnnotationTitle(element) + "\""))
                .collect(Collectors.toList());
    }
}

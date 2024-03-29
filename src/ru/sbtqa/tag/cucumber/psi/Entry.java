package ru.sbtqa.tag.cucumber.psi;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.intellij.psi.PsiModifierListOwner;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import ru.sbtqa.tag.editor.idea.utils.TagProjectUtils;

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
        return TagProjectUtils.getActionAnnotations(clazz).stream()
                .map(Pair::component1)
                .collect(Collectors.toList());
    }

    private List<PsiAnnotation> getElements() {
        List<PsiAnnotation> elementsList = new ArrayList<>();

        elementsList.addAll(getElementsAnnotations(clazz.getAllFields()));
        elementsList.addAll(getElementsAnnotations(clazz.getAllMethods()));

        return elementsList;
    }

    private List<PsiAnnotation> getElementsAnnotations(PsiModifierListOwner[] elements) {
        return Arrays.stream(elements)
                .map(TagProjectUtils::getElementAnnotation)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public String getTitle() {
        return TagProjectUtils.getAnnotationTitle(getAnnotation());
    }

    public PsiAnnotation getAnnotation() {
        PsiAnnotation endpointAnnotation = clazz.getAnnotation(TagProjectUtils.ENDPOINT_ANNOTATION_QUALIFIED_NAME);
        PsiAnnotation pageAnnotation = clazz.getAnnotation(TagProjectUtils.PAGE_ENTRY_ANNOTATION_QUALIFIED_NAME);

        return endpointAnnotation != null ? endpointAnnotation : pageAnnotation;
    }

    public List<PsiElement> getSupportsActions(String stepDef) {
        return actions.stream()
                .filter(action -> stepDef.contains("(" +  TagProjectUtils.getAnnotationTitle(action) + ")"))
                .collect(Collectors.toList());
    }

    public List<PsiElement> getSupportsElements(String stepDef) {
        return elements.stream()
                .filter(element -> stepDef.contains("\"" + TagProjectUtils.getAnnotationTitle(element) + "\""))
                .collect(Collectors.toList());
    }
}

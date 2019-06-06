package org.jetbrains.plugins.cucumber.steps;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.SmartPointerManager;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import ru.sbtqa.tag.editor.idea.utils.StringUtils;
import ru.sbtqa.tag.editor.idea.utils.TagProject;

public class Entry {

    private PsiElement element;
    private PsiClass clazz;
    private List<PsiAnnotation> actions;
    private List<PsiAnnotation> elements;

    public Entry(@NotNull final PsiElement element) {
        this.element = element;
        this.clazz = (PsiClass) element;
        this.actions = getActions();
        this.elements = getElements();
    }

    public String getTitle() {
        return StringUtils.unquote(TagProject.getAnnotationTitle(getClassAnnotation()));
    }

    public PsiElement getEntryAnnotation() {
        return SmartPointerManager.getInstance(element.getProject()).createSmartPsiElementPointer(getClassAnnotation()).getElement();
    }

    private PsiAnnotation getClassAnnotation() {
        PsiAnnotation endpointAnnotation = clazz.getAnnotation(TagProject.ENDPOINT_ANNOTATION_QUALIFIED_NAME);
        PsiAnnotation pageAnnotation = clazz.getAnnotation(TagProject.PAGE_ENTRY_ANNOTATION_QUALIFIED_NAME);

        return endpointAnnotation != null ? endpointAnnotation : pageAnnotation;
    }

    private List<PsiAnnotation> getActions() {
        return TagProject.getActionAnnotations(clazz).stream()
                .map(Pair::component1)
                .collect(Collectors.toList());
    }


    public List<PsiElement> getSupportsActions(String stepDef) {
        return actions.stream()
                .map(annotation -> {
                    String annotationTitle = StringUtils.unquote(TagProject.getAnnotationTitle(annotation));

                    if (stepDef.contains("(" + annotationTitle + ")")) {
                        return SmartPointerManager.getInstance(annotation.getProject()).createSmartPsiElementPointer(annotation).getElement();
                    }
                    return null;
                })
                .distinct()
                .collect(Collectors.toList());
    }

    // TODO добавить апи методы
    private List<PsiAnnotation> getElements() {
        List<PsiAnnotation> list = new ArrayList<>();
        for (PsiField field : clazz.getAllFields()) {
            if (TagProject.isAnnotated(field)) {
                if (TagProject.hasTitledAnnotation(field) && TagProject.getAnnotation(field) != null) {
                    PsiAnnotation annotation = TagProject.getAnnotation(field);
                    PsiAnnotation psiAnnotation = SmartPointerManager.getInstance(annotation.getProject()).createSmartPsiElementPointer(annotation).getElement();
                    list.add(psiAnnotation);
                }
            }
        }
        return list;
    }

    public List<PsiElement> getSupportsElements(String stepDef) {
        return elements.stream()
                .filter(field -> stepDef.contains("\"" + StringUtils.unquote(TagProject.getAnnotationTitle(field)) + "\""))
                .collect(Collectors.toList());
    }
}

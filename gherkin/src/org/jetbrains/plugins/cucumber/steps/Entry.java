package org.jetbrains.plugins.cucumber.steps;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.completion.TagContext;
import ru.sbtqa.tag.editor.idea.utils.StringUtils;
import ru.sbtqa.tag.editor.idea.utils.TagProject;

public class Entry {

    private PsiElement element;
    private PsiClass clazz;

    public Entry(@NotNull final PsiElement element) {
        this.element = element;
        this.clazz = (PsiClass) element;
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

    public List<PsiElement> getSupportsActions(String stepDef) {
        return TagProject.getActionAnnotations(clazz).stream()
                .map(psiAnnotationIntegerPair -> {
                    PsiAnnotation annotation = psiAnnotationIntegerPair.component1();
                    String annotationTitle = StringUtils.unquote(TagProject.getAnnotationTitle(annotation));

                    if (stepDef.contains("(" + annotationTitle + ")")) {
                        return SmartPointerManager.getInstance(annotation.getProject()).createSmartPsiElementPointer(annotation).getElement();
                    }
                    return null;
                })
                .distinct()
                .collect(Collectors.toList());
    }

    public List<PsiElement> getSupportsElements(String stepDef, TagContext context) {
        return TagProject.getElementsss(context).stream()
                .filter(field -> stepDef.contains("\"" + StringUtils.unquote(TagProject.getTitleee(field)) + "\""))
                .map(field -> field.getAnnotation(TagProject.ELEMENT_ANNOTATION_QUALIFIED_NAME))
                .map(annotation -> SmartPointerManager.getInstance(annotation.getProject()).createSmartPsiElementPointer(annotation).getElement())
                .collect(Collectors.toList());
    }
}

package ru.sbtqa.tag.editor.idea.utils;

import com.intellij.openapi.module.Module;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.intellij.util.Query;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import kotlin.Pair;
import org.jetbrains.plugins.cucumber.completion.TagCompletionElement;
import org.jetbrains.plugins.cucumber.completion.TagContext;

public class TagProject {

    public static final String ENDPOINT_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.pagefactory.annotations.rest.Endpoint";
    public static final String PAGE_ENTRY_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.pagefactory.annotations.PageEntry";
    public static final String ELEMENT_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.pagefactory.annotations.ElementTitle";
    public static final String VALIDATION_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.api.annotation.Validation";

    private static final String ACTION_TITLE_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.pagefactory.annotations.ActionTitle";
    private static final String ACTION_TITLES_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.pagefactory.annotations.ActionTitles";

    private static final String QUERY_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.api.annotation.Query";
    private static final String BODY_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.api.annotation.Body";
    private static final String COOKIE_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.api.annotation.Cookie";
    private static final String FROMRESPONSE_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.api.annotation.FromResponse";
    private static final String HEADER_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.api.annotation.Header";

    private static final String VALUE = "value";
    private static final String TITLE = "title";
    private static final String NAME = "name";

    private static ArrayList<String> elementAnnotations = new ArrayList() {
        {
            add(VALIDATION_ANNOTATION_QUALIFIED_NAME);
            add(ELEMENT_ANNOTATION_QUALIFIED_NAME);
            add(QUERY_ANNOTATION_QUALIFIED_NAME);
            add(BODY_ANNOTATION_QUALIFIED_NAME);
            add(COOKIE_ANNOTATION_QUALIFIED_NAME);
            add(FROMRESPONSE_ANNOTATION_QUALIFIED_NAME);
            add(HEADER_ANNOTATION_QUALIFIED_NAME);
        }
    };

    /**
     * Найти все экшены на странице
     */
    public static List<TagCompletionElement> getActionTitles(TagContext context) {
        return getActionAnnotations(context.getUi()).stream()
                .map(psiAnnotationIntegerPair -> new TagCompletionElement(getAnnotationTitle(psiAnnotationIntegerPair.component1()), psiAnnotationIntegerPair.component1().getQualifiedName()) )
                .collect(Collectors.toList());
    }

    public static List<Pair<PsiAnnotation, Integer>> getActionAnnotations(PsiClass page) {
        List<Pair<PsiAnnotation, PsiMethod>> allAnnotations = new ArrayList<>();

        Arrays.stream(page.getAllMethods())
                .filter(method -> method.getContainingClass() != null && method.getContainingClass().getQualifiedName() != null)
                .forEach(method -> Arrays.stream(method.getModifierList().getAnnotations())
                        .forEach(annotation -> allAnnotations.add(new Pair<>(annotation, method))));

        List<Pair<PsiAnnotation, Integer>> actionTitleAnnotations = new ArrayList<>();

        actionTitleAnnotations.addAll(allAnnotations.stream()
                .filter(x -> x.getFirst() != null && ACTION_TITLE_ANNOTATION_QUALIFIED_NAME.equals(x.getFirst().getQualifiedName()))
                .map(x -> new Pair<>(x.getFirst(), x.getSecond().getParameterList().getParameters().length))
                .collect(Collectors.toList()));

        final List<Pair<PsiAnnotation, Integer>> actionTitleArrays = allAnnotations.stream()
                .filter(x -> x.getFirst() != null && ACTION_TITLES_ANNOTATION_QUALIFIED_NAME.equals(x.getFirst().getQualifiedName()))
                .map(x -> new Pair<>(x.getFirst(), x.getSecond().getParameterList().getParameters().length))
                .collect(Collectors.toList());
        actionTitleArrays.forEach(x -> actionTitleAnnotations.addAll(Arrays.asList(x.component1().findAttributeValue(VALUE).getChildren())
                .stream().filter(psiElement -> psiElement instanceof PsiAnnotation)
                .map(y -> new Pair<>((PsiAnnotation) y, x.getSecond()))
                .collect(Collectors.toList())));

        return actionTitleAnnotations;
    }

    private static List<TagCompletionElement> getApiMethods(TagContext context) {
        if (context.getApi() == null) {
            return new ArrayList<>();
        }

        return Arrays.stream(context.getApi().getAllMethods())
                .filter(method -> method.getContainingClass() != null && method.getContainingClass().getQualifiedName() != null)
                .filter(TagProject::isAnnotated)
                .map(TagProject::getTitle)
                .filter(completionElement -> StringUtils.isNotBlank(completionElement.getPresentableText()))
                .collect(Collectors.toList());
    }

    private static boolean isAnnotated(PsiMethod method) {
        return elementAnnotations.stream().anyMatch(method::hasAnnotation);
    }

    public static boolean isAnnotated(PsiField field) {
        return elementAnnotations.stream().anyMatch(field::hasAnnotation);
    }

    /**
     * Найти все элементы на странице
     */
    public static List<TagCompletionElement> getElements(TagContext context) {
        ArrayList<PsiField> allFields = new ArrayList<>();
        if (context.getApi() != null) {
            allFields.addAll(Arrays.asList(context.getApi().getAllFields()));
        }
        if (context.getUi() != null) {
            allFields.addAll(Arrays.asList(context.getUi().getAllFields()));
        }

        List<TagCompletionElement> elements = allFields.stream()
                .filter(TagProject::isAnnotated)
                .map(TagProject::getTitle)
                .filter(completionElement -> StringUtils.isNotBlank(completionElement.getPresentableText()))
                .collect(Collectors.toList());

        return Stream
                .concat(elements.stream(), getApiMethods(context).stream())
                .collect(Collectors.toList());
    }

    private static TagCompletionElement getTitle(PsiModifierListOwner element) {
        String annotationFqdn = elementAnnotations.stream().filter(element::hasAnnotation).findFirst().orElse("");
        String title = getAnnotationTitle(element.getAnnotation(annotationFqdn));

        return new TagCompletionElement(title, annotationFqdn);
    }

    public static boolean hasTitledAnnotation(PsiModifierListOwner element) {
        for (PsiAnnotation annotation : element.getAnnotations()) {
            if (!getAnnotationTitle(annotation).equals("")) {
                return true;
            }
        }
        return false;
    }

    public static String getAnnotationTitle(PsiAnnotation annotation) {
        if (annotation != null) {
            if (annotation.findAttributeValue(NAME) != null) {
                return annotation.findAttributeValue(NAME).getText();
            } else if (annotation.findAttributeValue(TITLE) != null) {
                return annotation.findAttributeValue(TITLE).getText();
            } else if (annotation.findAttributeValue(VALUE) != null) {
                return annotation.findAttributeValue(VALUE).getText();
            }
        }

        return "";
    }

    public static PsiAnnotation getAnnotation(PsiModifierListOwner element) {
        for (PsiAnnotation annotation : element.getAnnotations()) {
        if (annotation != null && (annotation.findAttributeValue(NAME) != null
                    || annotation.findAttributeValue(TITLE) != null
                    || annotation.findAttributeValue(VALUE) != null)) {
                return annotation;
            }
        }

        return null;
    }

    /**
     * Найти значения title() для аннотации PageEntry
     */
    public static String findPageName(PsiClass page) {
        String annotationTitle = page.getAnnotation(PAGE_ENTRY_ANNOTATION_QUALIFIED_NAME).findAttributeValue(TITLE).getText();

        return StringUtils.unquote(annotationTitle);
    }

    /**
     * Найти значения title() для аннотации PageEntry
     */
    public static String findEndpointName(PsiClass endpoint) {
        String annotationTitle = endpoint.getAnnotation(ENDPOINT_ANNOTATION_QUALIFIED_NAME).findAttributeValue(TITLE).getText();

        return StringUtils.unquote(annotationTitle);
    }

    /**
     * Поиск всех страниц в проекте имеющих аннотацию PageEntry
     */
    public static Stream<PsiClass> getPages(Module module) {
        return getEntries(module, PAGE_ENTRY_ANNOTATION_QUALIFIED_NAME);
    }

    /**
     * Поиск всех страниц в проекте имеющих аннотацию Endpoint
     */
    public static Stream<PsiClass> getEndpoints(Module module) {
        return getEntries(module, ENDPOINT_ANNOTATION_QUALIFIED_NAME);
    }

    /**
     * TODO
     * @param module
     * @param annotation
     * @return
     */
    public static Stream<PsiClass> getEntries(Module module, String annotation) {
        PsiClass pageEntry = JavaPsiFacade.getInstance(module.getProject())
                .findClass(annotation, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module));
        if (pageEntry != null) {
            Query<PsiClass> psiClasses = AnnotatedElementsSearch.searchPsiClasses(pageEntry, GlobalSearchScope.moduleScope(module));
            return psiClasses.findAll().stream();
        } else {
            return Stream.empty();
        }
    }

    /**
     * Поиск страницы по имени
     */
    public static PsiClass getPageByName(Module module, String pageTitle) {
        return getPages(module)
                .filter(pageClass -> findPageName(pageClass).equals(pageTitle))
                .findFirst()
                .orElse(null);
    }

    public static PsiClass getEndpointByName(Module module, String endpointTitle) {
        return getEndpoints(module)
                .filter(entryClass -> findEndpointName(entryClass).equals(endpointTitle))
                .findFirst()
                .orElse(null);
    }
}

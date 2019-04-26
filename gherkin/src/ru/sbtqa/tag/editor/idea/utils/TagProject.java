package ru.sbtqa.tag.editor.idea.utils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
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

    private static final String ACTION_TITLE_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.pagefactory.annotations.ActionTitle";
    private static final String ACTION_TITLES_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.pagefactory.annotations.ActionTitles";
    private static final String PAGE_ENTRY_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.pagefactory.annotations.PageEntry";
    private static final String ELEMENT_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.pagefactory.annotations.ElementTitle";

    private static final String ENDPOINT_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.api.annotation.Endpoint";
    private static final String VALIDATION_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.api.annotation.Validation";
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

    private TagProject() {}

    /**
     * Найти все экшены на странице
     */
    public static List<TagCompletionElement> getActionTitles(TagContext context) {
        return getActionAnnotations(context.getUi()).stream()
                .map(psiAnnotationIntegerPair -> new TagCompletionElement(getAnnotationTitle(psiAnnotationIntegerPair.component1()), psiAnnotationIntegerPair.component1().getQualifiedName()) )
                .collect(Collectors.toList());
    }

    private static List<Pair<PsiAnnotation, Integer>> getActionAnnotations(PsiClass page) {
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
                .collect(Collectors.toList());
    }

    private static boolean isAnnotated(PsiMethod method) {
        return elementAnnotations.stream().anyMatch(method::hasAnnotation);
    }

    private static boolean isAnnotated(PsiField field) {
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
                .collect(Collectors.toList());

        return Stream
                .concat(elements.stream(), getApiMethods(context).stream())
                .collect(Collectors.toList());
    }

    private static TagCompletionElement getTitle(PsiMethod method) {
        String annotationFqdn = elementAnnotations.stream().filter(method::hasAnnotation).findFirst().orElse("");
        String title = getAnnotationTitle(method.getAnnotation(annotationFqdn));
        return new TagCompletionElement(title, annotationFqdn);
    }

    private static TagCompletionElement getTitle(PsiField field) {
        String annotationFqdn = elementAnnotations.stream().filter(field::hasAnnotation).findFirst().orElse("");
        String title = getAnnotationTitle(field.getAnnotation(annotationFqdn));
        return new TagCompletionElement(title, annotationFqdn);
    }

    private static String getAnnotationTitle(PsiAnnotation annotation) {
        if (annotation != null) {
            if (annotation.findAttributeValue(NAME) != null) {
                return annotation.findAttributeValue(NAME).getText();
            } else if (annotation.findAttributeValue(TITLE) != null) {
                return annotation.findAttributeValue(TITLE).getText();
            } else {
                return annotation.findAttributeValue(VALUE).getText();
            }
        }
        return "";
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
    public static Stream<PsiClass> getPages(Project project) {
        return getEntries(project, PAGE_ENTRY_ANNOTATION_QUALIFIED_NAME);
    }

    /**
     * Поиск всех страниц в проекте имеющих аннотацию Endpoint
     */
    public static Stream<PsiClass> getEndpoints(Project project) {
        return getEntries(project, ENDPOINT_ANNOTATION_QUALIFIED_NAME);
    }

    private static Stream<PsiClass> getEntries(Project project, String annotation) {
        PsiClass pageEntry = JavaPsiFacade.getInstance(project)
                .findClass(annotation, GlobalSearchScope.everythingScope(project));
        Query<PsiClass> psiClasses = AnnotatedElementsSearch.searchPsiClasses(pageEntry, GlobalSearchScope.projectScope(project));

        return psiClasses.findAll().stream();
    }

    /**
     * Поиск страницы по имени
     */
    public static PsiClass getPageByName(Project project, String pageTitle) {
        return getPages(project)
                .filter(pageClass -> findPageName(pageClass).equals(pageTitle))
                .findFirst()
                .orElse(null);
    }

    public static PsiClass getEndpointByName(Project project, String endpointTitle) {
        return getEndpoints(project)
                .filter(entryClass -> findEndpointName(entryClass).equals(endpointTitle))
                .findFirst()
                .orElse(null);
    }
}

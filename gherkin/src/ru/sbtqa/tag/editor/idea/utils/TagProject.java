package ru.sbtqa.tag.editor.idea.utils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
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

public class TagProject {

    private static final String ACTION_TITLE_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.pagefactory.annotations.ActionTitle";
    private static final String ACTION_TITLES_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.pagefactory.annotations.ActionTitles";
    private static final String PAGE_ENTRY_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.pagefactory.annotations.PageEntry";
    private static final String ELEMENT_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.pagefactory.annotations.ElementTitle";

    private static final String VALUE = "value";
    private static final String TITLE = "title";

    private TagProject() {}

    /**
     * Найти все экшены на странице
     */
    public static List<String> getActionTitles(PsiClass page) {
        return getActionAnnotations(page).stream()
                .map(psiAnnotationIntegerPair -> psiAnnotationIntegerPair.component1().findAttributeValue(VALUE).getText())
                .map(StringUtils::unquote)
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

    /**
     * Найти все элементы на странице
     */
    public static List<String> getElements(PsiClass page) {
        return Arrays.stream(page.getAllFields())
                .filter(field -> field.hasAnnotation(ELEMENT_ANNOTATION_QUALIFIED_NAME))
                .map(field -> field.getAnnotation(ELEMENT_ANNOTATION_QUALIFIED_NAME).findAttributeValue(VALUE).getText())
                .map(StringUtils::unquote)
                .collect(Collectors.toList());
    }

    /**
     * Найти значения title() для аннотации PageEntry
     */
    public static String findPageName(PsiClass page) {
        String annotationTitle = page.getAnnotation(PAGE_ENTRY_ANNOTATION_QUALIFIED_NAME).findAttributeValue(TITLE).getText();

        return StringUtils.unquote(annotationTitle);
    }

    /**
     * Поиск всех страниц в проекте имеющих аннотацию PageEntry
     */
    public static Stream<PsiClass> getPages(Project project) {
        PsiClass pageEntry = JavaPsiFacade.getInstance(project)
                .findClass(PAGE_ENTRY_ANNOTATION_QUALIFIED_NAME, GlobalSearchScope.everythingScope(project));
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
}

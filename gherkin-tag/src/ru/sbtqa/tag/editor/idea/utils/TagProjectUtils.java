package ru.sbtqa.tag.editor.idea.utils;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.intellij.util.Query;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import kotlin.Pair;
import org.jetbrains.plugins.cucumber.psi.*;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinStepsHolderBase;

public class TagProjectUtils {

    public static final String ENDPOINT_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.pagefactory.annotations.rest.Endpoint";
    public static final String PAGE_ENTRY_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.pagefactory.annotations.PageEntry";
    public static final String VALIDATION_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.api.annotation.Validation";

    private static final String ELEMENT_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.pagefactory.annotations.ElementTitle";
    private static final String ACTION_TITLE_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.pagefactory.annotations.ActionTitle";
    private static final String ACTION_TITLES_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.pagefactory.annotations.ActionTitles";

    private static final String QUERY_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.api.annotation.Query";
    private static final String BODY_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.api.annotation.Body";
    private static final String COOKIE_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.api.annotation.Cookie";
    private static final String FROMRESPONSE_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.api.annotation.FromResponse";
    private static final String HEADER_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.api.annotation.Header";

    private static final Pattern QUOTES_VALUE_EXTRACTOR_PATTERN = Pattern.compile("\"([^\"]*)\"");

    private static final String VALUE = "value";
    private static final String TITLE = "title";
    private static final String NAME = "name";

    public static ArrayList<String> elementAnnotations = new ArrayList() {
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

    public static boolean isAnnotated(PsiMethod method) {
        return elementAnnotations.stream().anyMatch(method::hasAnnotation);
    }

    public static boolean isAnnotated(PsiField field) {
        return elementAnnotations.stream().anyMatch(field::hasAnnotation);
    }

    public static boolean hasTitledAnnotation(PsiModifierListOwner element) {
        for (PsiAnnotation annotation : element.getAnnotations()) {
            if (!getAnnotationTitle(annotation).equals(StringUtils.EMPTY_STRING)) {
                return true;
            }
        }
        return false;
    }

    public static String getAnnotationTitle(PsiAnnotation annotation) {
        String title = StringUtils.EMPTY_STRING;
        if (annotation != null) {
            if (annotation.findAttributeValue(NAME) != null) {
                title = annotation.findAttributeValue(NAME).getText();
            } else if (annotation.findAttributeValue(TITLE) != null) {
                title = annotation.findAttributeValue(TITLE).getText();
            } else if (annotation.findAttributeValue(VALUE) != null) {
                title = annotation.findAttributeValue(VALUE).getText();
            }
        }
        return StringUtils.unquote(title);
    }

    public static Optional<PsiAnnotation> getElementAnnotation(PsiModifierListOwner element) {
        return Arrays.stream(element.getAnnotations())
                .filter(annotation ->
                        elementAnnotations.stream().anyMatch(elementAnnotation -> elementAnnotation.equals(annotation.getQualifiedName())))
                .findFirst();
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
     * Поиск всех страниц в проекте по заданной аннотации
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
     * Поиск страницы имеющей аннотацию PageEntry по имени
     */
    public static PsiClass getPageByName(Module module, String pageTitle) {
        return getPages(module)
                .filter(pageClass -> findPageName(pageClass).equals(pageTitle))
                .findFirst()
                .orElse(null);
    }

    /**
     * Поиск страницы имеющей аннотацию Endpoint по имени
     */
    public static PsiClass getEndpointByName(Module module, String endpointTitle) {
        return getEndpoints(module)
                .filter(entryClass -> findEndpointName(entryClass).equals(endpointTitle))
                .findFirst()
                .orElse(null);
    }

    /**
     * Получить тайтл по регэкспу
     */
    public static String parseTitle(String step) {
        Matcher matcher = QUOTES_VALUE_EXTRACTOR_PATTERN.matcher(step);
        if (matcher.find()) {
            return matcher.group().replaceAll(StringUtils.QUOTE, StringUtils.EMPTY_STRING);
        }
        return StringUtils.EMPTY_STRING;
    }

    /**
     * Поиск всех страниц в проекте по заданной аннотации
     */
    public static Stream<GherkinStepsHolder> getScenarios(Module module) {
        final List<GherkinStepsHolder> result = new ArrayList<>();

        GlobalSearchScope scope = GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.moduleScope(module), GherkinFileType.INSTANCE);

        for (VirtualFile virtualFile : FilenameIndex.getAllFilesByExt(scope.getProject(), "feature", scope)) {
            GherkinFile gherkinFile = (GherkinFile) PsiManager.getInstance(scope.getProject()).findFile(virtualFile);
            for (GherkinFeature feature : gherkinFile.getFeatures()) {
                for (GherkinStepsHolder scenario : feature.getScenarios()) {
                    // TODO проверить что сценарий имеет тэг @fragment
                    for (GherkinTag tag : scenario.getTags()) {
                        if (tag.getName().equals("@fragment")) {
                            result.add(scenario);
                        }
                    }

                }
            }
        }

        return result.stream();
    }
}

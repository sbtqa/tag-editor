package ru.sbtqa.tag.editor.idea.utils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.intellij.util.Query;
import kotlin.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by cyberspace on 7/19/2017.
 */
public class TagProject {

    public static final String RU_LANGUAGE = "ru";
    public static final String EN_LANGUAGE = "en";

    public static final String ACTION_TITLE_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.pagefactory.annotations.ActionTitle";
    public static final String ACTION_TITLES_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.pagefactory.annotations.ActionTitles";
    public static final String PAGE_ENTRY_ANNOTATION_QUALIFIED_NAME = "ru.sbtqa.tag.pagefactory.annotations.PageEntry";
    public static final String TAG_PAGE_QUALIFIED_NAME = "ru.sbtqa.tag.pagefactory.Page";

    private TagProject() {
    }

    /**
     * Найти все аннотации ActionTitle класса наследуемого от ru.sbtqa.tag.pagefactory.Page.
     *
     * @param page
     * @return Возвращает поток пар значений (Аннотация; Количество параметров в сигнатуре метода c данной аннотацией).
     */
    public static List<Pair<PsiAnnotation, Integer>> actionAnnotations(PsiClass page) {
        List<Pair<PsiAnnotation, PsiMethod>> allAnnotations = new ArrayList<>();
        Arrays.stream(page.getAllMethods())
                .filter(x -> x.getContainingClass() != null &&
                        x.getContainingClass().getQualifiedName() != null) // игнорируем экшины из родителя FIXME: а надо ли?
                .forEach(x -> Arrays.stream(x.getModifierList().getAnnotations())
                        .forEach(y -> allAnnotations.add(new Pair<>(y, x))));


        List<Pair<PsiAnnotation, Integer>> actionTitleAnnotations = new ArrayList<>();


        actionTitleAnnotations.addAll(allAnnotations.stream()
                .filter(x -> x.getFirst() != null && ACTION_TITLE_ANNOTATION_QUALIFIED_NAME.equals(x.getFirst().getQualifiedName()))
                .map(x -> new Pair<>(x.getFirst(), x.getSecond().getParameterList().getParameters().length)).collect(Collectors.toList()));



        final List<Pair<PsiAnnotation, Integer>> actionTitleArrays = allAnnotations.stream()
                .filter(x -> x.getFirst() != null && ACTION_TITLES_ANNOTATION_QUALIFIED_NAME.equals(x.getFirst().getQualifiedName()))
                .map(x -> new Pair<>(x.getFirst(), x.getSecond().getParameterList().getParameters().length)).collect(Collectors.toList());
        actionTitleArrays.forEach(x -> actionTitleAnnotations.addAll(Arrays.asList(x.component1().findAttributeValue("value").getChildren())
                .stream().filter(psiElement -> psiElement instanceof PsiAnnotation)
                .map(y -> new Pair<>((PsiAnnotation) y, x.getSecond())).collect(Collectors.toList())));


        return actionTitleAnnotations;
    }


    public static List<String> actionTitles(PsiClass page) {
        return actionAnnotations(page).stream().map(psiAnnotationIntegerPair -> {
            final String value = psiAnnotationIntegerPair.component1().findAttributeValue("value").getText();
            return value.substring(1, value.length() - 1);
        }).collect(Collectors.toList());
    }

//    /**
//     * Опредялеят если в классе метод, который содержит заданный actionTitle
//     */
//    public static PsiMethod containsActionTitle(PsiClass psiClass, String actionTitle, Project project) {
//        List<PsiMethod> methods = new ArrayList<>();
//        while (psiClass != null) {
//            methods.addAll(Arrays.asList(psiClass.getAllMethods()));
//            psiClass = psiClass.getSuperClass();
//        }
//        final PsiConstantEvaluationHelper evaluationHelper = JavaPsiFacade.getInstance(project).getConstantEvaluationHelper();
//        for (PsiMethod method : methods) {
//            List<PsiAnnotation> psiAnnotations = Optional.of(method.getModifierList()).map(PsiAnnotationOwner::getAnnotations).map(Arrays::asList).orElse(null);
//            if (psiAnnotations != null) {
//                PsiAnnotation actionTileAnnotation = psiAnnotations.stream()
//                        .filter(x -> TagProject.ACTION_TITLE_ANNOTATION_QUALIFIED_NAME.equals(x.getQualifiedName()))
//                        .findFirst()
//                        .orElse(null);
//                String actionTitleOfAnnotation = Optional.ofNullable(actionTileAnnotation)
//                        .map(CucumberJavaUtil::getAnnotationValue)
//                        .map(y -> evaluationHelper.computeConstantExpression(y, false))
//                        .map(Object::toString)
//                        .filter(y -> y.length() > 1)
//                        .orElse(null);
//                if (actionTitle.equals(actionTitleOfAnnotation)) {
//                    return method;
//                }
//            }
//        }
//        return null;
//    }

    /**
     * Найти значаение title() для аннотации PageEntry для класса наследованного от ru.sbtqa.tag.pagefactory.Page.
     *
     * @param page
     * @return
     */
    public static String findPageName(PsiClass page) {
        String annotationTitle = page.getAnnotation(PAGE_ENTRY_ANNOTATION_QUALIFIED_NAME).findAttribute("title")
                .getAttributeValue().getSourceElement().getText();
        return annotationTitle.substring(1, annotationTitle.length() - 1);

    }

    /**
     * Поиск всех имеющих предка ru.sbtqa.tag.pagefactory.Page.
     *
     * @param project
     * @return
     */
    public static Stream<PsiClass> pages(Project project) {

        final PsiClass aClass = JavaPsiFacade.getInstance(project).findClass(PAGE_ENTRY_ANNOTATION_QUALIFIED_NAME,
                GlobalSearchScope.projectScope(project));
        final Query<PsiClass> psiClasses = AnnotatedElementsSearch.searchPsiClasses(aClass, GlobalSearchScope.projectScope(project));
        return psiClasses.findAll().stream();

    }


    /**
     * Поиск страницы по имени
     *
     * @param project
     * @return
     */
    public static PsiClass getPageByName(Project project, String pageTitle) {
        return pages(project).filter(pageClass ->
                findPageName(pageClass).equals(pageTitle)
        ).findFirst().orElse(null);
    }

    /**
     * Имеет ли класс в наследниках ru.sbtqa.tag.pagefactory.Page.
     *
     * @param psiClass
     * @return
     */
    private static boolean isTAGPage(PsiClass psiClass) {
        while (psiClass != null) {
            if (TAG_PAGE_QUALIFIED_NAME.equals(psiClass.getQualifiedName()))
                return true;
            psiClass = psiClass.getSuperClass();
        }
        return false;
    }

}

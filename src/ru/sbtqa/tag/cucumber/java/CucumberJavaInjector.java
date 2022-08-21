package ru.sbtqa.tag.cucumber.java;

import com.intellij.lang.Language;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import ru.sbtqa.tag.cucumber.CucumberUtil;
import ru.sbtqa.tag.cucumber.java.CucumberJavaUtil;

public class CucumberJavaInjector implements MultiHostInjector {

    public static final Language regexpLanguage = Language.findLanguageByID("RegExp");

    @Override
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement element) {
        if (regexpLanguage == null) {
            return;
        }
        if (element instanceof PsiLiteralExpression && element instanceof PsiLanguageInjectionHost && element.getTextLength() > 2) {
            final PsiElement firstChild = element.getFirstChild();
            if (firstChild != null && firstChild.getNode().getElementType() == JavaTokenType.STRING_LITERAL) {
                PsiAnnotation annotation = PsiTreeUtil.getParentOfType(element, PsiAnnotation.class);
                if (annotation != null &&
                        (CucumberJavaUtil.isCucumberStepAnnotation(annotation) || CucumberJavaUtil.isCucumberHookAnnotation(annotation))) {
                    final TextRange range = new TextRange(1, element.getTextLength() - 1);
                    String stepDefinitionPattern = range.substring(element.getText());
                    if (!CucumberUtil.isCucumberExpression(stepDefinitionPattern)) {
                        registrar.startInjecting(regexpLanguage).addPlace(null, null, (PsiLanguageInjectionHost) element, range).doneInjecting();
                    }
                }
            }
        }
    }

    @NotNull
    @Override
    public List<Class<? extends PsiElement>> elementsToInjectIn() {
        return Collections.singletonList(PsiLiteralExpression.class);
    }
}

package org.jetbrains.plugins.cucumber.completion;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinStepImpl;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import ru.sbtqa.tag.editor.idea.utils.TagProject;

public class TagContext {

    private final static Pattern QUOTES_VALUE_EXTRACTOR_PATTERN = Pattern.compile("\"([^\"]*)\"");

    private PsiElement currentElement;
    private PsiClass api;
    private PsiClass ui;

    public TagContext(PsiElement currentElement) {
        this.currentElement = currentElement;
        this.api = getCurrentEndpoint();
        this.ui = getCurrentPage();
    }

    public PsiClass getApi() {
        return api;
    }

    public PsiClass getUi() {
        return ui;
    }

    public boolean isEmpty() {
        return api == null && ui == null;
    }

    private PsiClass getCurrentPage() {
        return TagProject.getPageByName(currentElement.getProject(), getCurrentTitle(true));
    }

    private PsiClass getCurrentEndpoint() {
        return TagProject.getEndpointByName(currentElement.getProject(), getCurrentTitle(false));
    }

    private String getCurrentTitle(boolean isUi) {
        PsiElement prevElement = currentElement;
        do {
            GherkinStepImpl prevStep = (prevElement instanceof GherkinStepImpl) ? (GherkinStepImpl) prevElement : null;
            if (prevStep != null) {
                boolean isStepChanger = isUi ? prevStep.findDefinitions().stream().anyMatch(AbstractStepDefinition::isUiContextChanger)
                        : prevStep.findDefinitions().stream().anyMatch(AbstractStepDefinition::isApiContextChanger);
                if (isStepChanger) {
                    return getTitle(prevStep.getName());
                }
            }
        } while ((prevElement = prevElement.getPrevSibling()) != null);

        return "";
    }

    private String getTitle(String step) {
        Matcher matcher = QUOTES_VALUE_EXTRACTOR_PATTERN.matcher(step);
        if (matcher.find()) {
            return matcher.group().replaceAll("\"", "");
        }
        return "";
    }
}

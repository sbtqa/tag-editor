package ru.sbtqa.tag.cucumber.completion;

import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.lang3.ArrayUtils;
import ru.sbtqa.tag.cucumber.psi.GherkinFeature;
import ru.sbtqa.tag.cucumber.psi.GherkinScenario;
import ru.sbtqa.tag.cucumber.psi.GherkinStep;
import ru.sbtqa.tag.cucumber.psi.GherkinStepsHolder;
import ru.sbtqa.tag.cucumber.steps.AbstractStepDefinition;
import ru.sbtqa.tag.editor.idea.utils.TagProjectUtils;

public class TagContext {

    private GherkinStep[] background;
    private PsiElement currentElement;
    private PsiClass api;
    private PsiClass ui;

    public TagContext(PsiElement currentElement, PsiFile originalFile) {
        this.currentElement = currentElement;
        this.background = getBackground(originalFile);
        this.api = getCurrentEndpoint();
        this.ui = getCurrentPage();
    }

    private GherkinStep[] getBackground(PsiFile originalFile) {
        PsiElement scenarioParent = PsiTreeUtil.getChildOfType(originalFile, GherkinFeature.class);
        if (scenarioParent == null) {
            scenarioParent = originalFile;
        }
        final GherkinScenario[] scenarios = PsiTreeUtil.getChildrenOfType(scenarioParent, GherkinScenario.class);
        if (scenarios != null) {
            for (GherkinScenario scenario : scenarios) {
                if (scenario.isBackground()) {
                    return scenario.getSteps();
                }
            }
        }
        return ArrayUtils.toArray();
    }

    private PsiClass getCurrentEndpoint() {
        return TagProjectUtils.getEndpointByName(ModuleUtilCore.findModuleForPsiElement(currentElement), getCurrentTitle(false));
    }

    private PsiClass getCurrentPage() {
        return TagProjectUtils.getPageByName(ModuleUtilCore.findModuleForPsiElement(currentElement), getCurrentTitle(true));
    }

    public String getCurrentTitle(boolean isUi) {
        String currentTitle = getTitle(isUi, currentElement);
        GherkinStepsHolder stepsHolder = ((GherkinStep) currentElement).getStepHolder();

        if (!isBackground(stepsHolder)
                && currentTitle.isEmpty()
                && background.length > 0) {
            currentTitle = getTitle(isUi, background[background.length - 1]);
        }

        return currentTitle;
    }

    private boolean isBackground(GherkinStepsHolder stepsHolder) {
        if (!(stepsHolder instanceof GherkinScenario)) {
            return false;
        }

        return ((GherkinScenario) stepsHolder).isBackground();
    }

    private String getTitle(boolean isUi, PsiElement element) {
        do {
            GherkinStep prevStep = (element instanceof GherkinStep) ? (GherkinStep) element : null;
            if (prevStep != null) {
                boolean isStepChanger = isUi ? prevStep.findDefinitions().stream().anyMatch(AbstractStepDefinition::isUiContextChanger)
                        : prevStep.findDefinitions().stream().anyMatch(AbstractStepDefinition::isApiContextChanger);
                if (isStepChanger) {
                    return TagProjectUtils.parseTitle(prevStep.getName());
                }
            }
        } while ((element = element.getPrevSibling()) != null);

        return "";
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

    public boolean isContextChanger() {
        if (currentElement instanceof GherkinStep) {
            GherkinStep step = (GherkinStep) currentElement;

            boolean isUiContextChanger = false;
            boolean isApiContextChanger = false;

            if (!step.findDefinitions().isEmpty()) {
                isUiContextChanger = step.findDefinitions().stream().findFirst().get().isUiContextChanger();
                isApiContextChanger = step.findDefinitions().stream().findFirst().get().isApiContextChanger();
            }

            return isUiContextChanger || isApiContextChanger;
        } else {
            return false;
        }
    }
}

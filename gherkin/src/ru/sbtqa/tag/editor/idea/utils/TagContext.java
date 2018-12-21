package ru.sbtqa.tag.editor.idea.utils;

import com.intellij.psi.PsiElement;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinStepImpl;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by SBT-Tatciy-IO on 26.07.2017.
 */
public class TagContext {

    private final static Pattern QUOTES_VALUE_EXTRACTOR_PATTERN = Pattern.compile("\"([^\"]*)\"");
    private final static Pattern BRACKETS_VALUE_EXTRACTOR_PATTERN = Pattern.compile("\\((.*?)\\)");
//    private final GherkinFileImpl gherkinFile;
//    private final GherkinStepImpl gherkinStep;

//    public TagContext(GherkinStepImpl step) {
//        this.gherkinStep = step;
//        this.gherkinFile = (GherkinFileImpl) step.getContainingFile();
//    }

//    public String getCurrentActionTitle() {
//        PsiElement prevElement = gherkinStep;
//        String language = ((GherkinFileImpl) gherkinFile.getContainingFile()).getLocaleLanguage();
//        do {
//            if (prevElement instanceof GherkinStepImpl) {
//                String step = ((GherkinStepImpl)prevElement).getStepName();
//                if (step != null) {
//                    for (String action : TagSteps.ACTION_STEP_NAMES) {
//                        org.apache.oro.text.regex.Pattern pattern = AbstractStepDefinition.createPattern(TagSteps.find(language, action));
//                        if (pattern != null && new Perl5Matcher().contains(step, pattern)) {
//                            Matcher m = BRACKETS_VALUE_EXTRACTOR_PATTERN.matcher(step);
//                            if (m.find())
//                                return m.group().replaceAll("\\(", "").replaceAll("\\)", "");
//                        }
//                    }
//                }
//            }
//        } while ((prevElement = prevElement.getPrevSibling()) != null);
//        return "";
//    }

    public static String getCurrentPageName(GherkinStepImpl gherkinStep) {
        String result = "";
        PsiElement prevElement = gherkinStep;
        do {
            GherkinStepImpl prevStep = (prevElement instanceof GherkinStepImpl) ? (GherkinStepImpl) prevElement : null;
            if (prevStep != null) {
                boolean isStepChanger = prevStep.findDefinitions().stream().anyMatch(AbstractStepDefinition::isContextChanger);
                if (isStepChanger) {
                    Matcher m = QUOTES_VALUE_EXTRACTOR_PATTERN.matcher(prevStep.getName());
                    if (m.find()) {
                        result = m.group().replaceAll("\"", "");
                        break;
                    }
                }


            }
        } while ((prevElement = prevElement.getPrevSibling()) != null);
        return result;
    }

//    public GherkinFileImpl getGherkinFile() {
//        return gherkinFile;
//    }
//
//    public GherkinStepImpl getGherkinStep() {
//        return gherkinStep;
//    }
}

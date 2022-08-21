package ru.sbtqa.tag.cucumber.psi.formatter;

import com.intellij.application.options.CodeStyleAbstractConfigurable;
import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.psi.codeStyle.CodeStyleConfigurable;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider;
import org.jetbrains.annotations.NotNull;

/**
 * @author Rustam Vishnyakov
 */
public class GherkinCodeStyleSettingsProvider extends CodeStyleSettingsProvider {
    @Override
    public @NotNull CodeStyleConfigurable createConfigurable(@NotNull CodeStyleSettings settings, @NotNull CodeStyleSettings modelSettings) {
        return new CodeStyleAbstractConfigurable(settings, modelSettings, "Gherkin") {
            @Override
            protected CodeStyleAbstractPanel createPanel(CodeStyleSettings settings) {
                return new GherkinCodeStylePanel(getCurrentSettings(), settings);
            }

            @Override
            public String getHelpTopic() {
                return "reference.settingsdialog.codestyle.gherkin";
            }
        };
    }

    @Override
    public String getConfigurableDisplayName() {
        return "Gherkin";
    }
}

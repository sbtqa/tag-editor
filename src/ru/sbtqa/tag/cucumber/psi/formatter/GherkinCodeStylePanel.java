package ru.sbtqa.tag.cucumber.psi.formatter;

import com.intellij.application.options.TabbedLanguageCodeStylePanel;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import ru.sbtqa.tag.cucumber.psi.GherkinLanguage;

/**
 * @author Rustam Vishnyakov
 */
public class GherkinCodeStylePanel extends TabbedLanguageCodeStylePanel {
  protected GherkinCodeStylePanel(CodeStyleSettings currentSettings, CodeStyleSettings settings) {
    super(GherkinLanguage.INSTANCE, currentSettings, settings);
  }

  @Override
  protected void initTabs(CodeStyleSettings settings) {
    addIndentOptionsTab(settings);
  }
}

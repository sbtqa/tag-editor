package org.jetbrains.plugins.cucumber.psi;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberBundle;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Roman.Chernyatchik
 */
public class GherkinColorsPage implements ColorSettingsPage {

 private static final String DEMO_TEXT =
    "# language: en\n" +
    "  @wip @data=<datajack_param>$Data</datajack_param>\n" +
    "Feature: Cucumber Colors Settings Page\n" +
    "  In order to customize Gherkin language (*.feature files) highlighting\n" +
    "  Our users can use this settings preview pane\n" +
    "\n" +
    "  @wip @data=<datajack_param>$Data{users}</datajack_param>\n" +
    "  Scenario Outline: Different Gherkin language structures\n" +
    "    Given Some feature file with content\n" +
    "    \"\"\"\n" +
    "    Feature: Some feature\n" +
    "      Scenario: Some scenario\n" +
    "    \"\"\"\n" +
    "    And I want to add new cucumber step\n" +
    "    And Also a step with \"<string_param>string</string_param>\" and digit <digit_param>42</digit_param> parameter\n" +
    "    And Also a step with (<action_param>action</action_param>) \"<string_param>DataJack param in step " +
            "<datajack_param>$Data{users[0].name}</datajack_param></string_param>\" parameter <datajack_param>${users.name}</datajack_param>\n" +
    "    When I open <<outline_param>ruby_ide</outline_param>>\n" +
    "    Then Steps autocompletion feature will help me with all these tasks\n" +
    "\n" +
    "  Examples:\n" +
    "    | <th>ruby_ide</th> |\n" +
    "    | RubyMine |";

  private static final AttributesDescriptor[] ATTRS = new AttributesDescriptor[]{
    new AttributesDescriptor(CucumberBundle.message("color.settings.gherkin.text"), GherkinHighlighter.TEXT),
    new AttributesDescriptor(CucumberBundle.message("color.settings.gherkin.comment"), GherkinHighlighter.COMMENT),
    new AttributesDescriptor(CucumberBundle.message("color.settings.gherkin.keyword"), GherkinHighlighter.KEYWORD),
    new AttributesDescriptor(CucumberBundle.message("color.settings.gherkin.tag"), GherkinHighlighter.TAG),
    new AttributesDescriptor(CucumberBundle.message("color.settings.gherkin.pystring"), GherkinHighlighter.PYSTRING),
    new AttributesDescriptor(CucumberBundle.message("color.settings.gherkin.table.header.cell"), GherkinHighlighter.TABLE_HEADER_CELL),
    new AttributesDescriptor(CucumberBundle.message("color.settings.gherkin.table.cell"), GherkinHighlighter.TABLE_CELL),
    new AttributesDescriptor(CucumberBundle.message("color.settings.gherkin.table.pipe"), GherkinHighlighter.PIPE),
    new AttributesDescriptor(CucumberBundle.message("color.settings.gherkin.outline.param.substitution"), GherkinHighlighter.OUTLINE_PARAMETER_SUBSTITUTION),
    new AttributesDescriptor(CucumberBundle.message("color.settings.gherkin.regexp.param"), GherkinHighlighter.REGEXP_PARAMETER),
    new AttributesDescriptor(CucumberBundle.message("color.settings.gherkin.action.param"), GherkinHighlighter.GHERKIN_ACTION_PARAMETER),
    new AttributesDescriptor(CucumberBundle.message("color.settings.gherkin.datajack.param"), GherkinHighlighter.DATAJACK_PARAMETER),
    new AttributesDescriptor(CucumberBundle.message("color.settings.gherkin.digit.param"), GherkinHighlighter.GHERKIN_DIGIT_PARAMETER),
    new AttributesDescriptor(CucumberBundle.message("color.settings.gherkin.string.param"), GherkinHighlighter.GHERKIN_STRING_PARAMETER),

  };

  // Empty still
  private static final Map<String, TextAttributesKey> ADDITIONAL_HIGHLIGHT_DESCRIPTORS = new HashMap<>();
  static {
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("th", GherkinHighlighter.TABLE_HEADER_CELL);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("outline_param", GherkinHighlighter.OUTLINE_PARAMETER_SUBSTITUTION);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("regexp_param", GherkinHighlighter.REGEXP_PARAMETER);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("action_param", GherkinHighlighter.GHERKIN_ACTION_PARAMETER);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("digit_param", GherkinHighlighter.GHERKIN_DIGIT_PARAMETER);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("datajack_param", GherkinHighlighter.DATAJACK_PARAMETER);
    ADDITIONAL_HIGHLIGHT_DESCRIPTORS.put("string_param", GherkinHighlighter.GHERKIN_STRING_PARAMETER);
  }

  @Override
  @Nullable
  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return ADDITIONAL_HIGHLIGHT_DESCRIPTORS;
  }

  @Override
  @NotNull
  public String getDisplayName() {
    return CucumberBundle.message("color.settings.gherkin.name");
  }

  @Override
  @NotNull
  public Icon getIcon() {
    return icons.CucumberIcons.Cucumber;
  }

  @Override
  @NotNull
  public AttributesDescriptor[] getAttributeDescriptors() {
    return ATTRS;
  }

  @Override
  @NotNull
  public ColorDescriptor[] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @Override
  @NotNull
  public SyntaxHighlighter getHighlighter() {
    return new GherkinSyntaxHighlighter(new PlainGherkinKeywordProvider());
  }

  @Override
  @NotNull
  public String getDemoText() {
    return DEMO_TEXT;
  }
}

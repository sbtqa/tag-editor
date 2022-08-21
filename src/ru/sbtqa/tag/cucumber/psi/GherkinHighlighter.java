package ru.sbtqa.tag.cucumber.psi;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NonNls;

/**
 * @author Roman.Chernyatchik
 */
public final class GherkinHighlighter {
  @NonNls
  static final String COMMENT_ID = "GHERKIN_COMMENT";
  public static final TextAttributesKey COMMENT = TextAttributesKey.createTextAttributesKey(
    COMMENT_ID,
    DefaultLanguageHighlighterColors.DOC_COMMENT
  );

  @NonNls
  static final String KEYWORD_ID = "GHERKIN_KEYWORD";
  public static final TextAttributesKey KEYWORD = TextAttributesKey.createTextAttributesKey(
    KEYWORD_ID,
    DefaultLanguageHighlighterColors.KEYWORD
  );

  @NonNls
  static final String GHERKIN_OUTLINE_PARAMETER_SUBSTITUTION_ID = "GHERKIN_OUTLINE_PARAMETER_SUBSTITUTION";
  public static final TextAttributesKey OUTLINE_PARAMETER_SUBSTITUTION = TextAttributesKey.createTextAttributesKey(
    GHERKIN_OUTLINE_PARAMETER_SUBSTITUTION_ID,
    DefaultLanguageHighlighterColors.INSTANCE_FIELD
  );

  @NonNls
  static final String GHERKIN_TABLE_HEADER_CELL_ID = "GHERKIN_TABLE_HEADER_CELL";
  public static final TextAttributesKey TABLE_HEADER_CELL = TextAttributesKey.createTextAttributesKey(
    GHERKIN_TABLE_HEADER_CELL_ID,
    OUTLINE_PARAMETER_SUBSTITUTION
  );

  @NonNls
  static final String GHERKIN_TAG_ID = "GHERKIN_TAG";
  public static final TextAttributesKey TAG = TextAttributesKey.createTextAttributesKey(
    GHERKIN_TAG_ID,
    DefaultLanguageHighlighterColors.METADATA
  );

  @NonNls
  static final String GHERKIN_REGEXP_PARAMETER_ID = "GHERKIN_REGEXP_PARAMETER";
  public static final TextAttributesKey REGEXP_PARAMETER = TextAttributesKey.createTextAttributesKey(
    GHERKIN_REGEXP_PARAMETER_ID,
    DefaultLanguageHighlighterColors.PARAMETER
  );

  @NonNls
  static final String GHERKIN_ACTION_PARAMETER_ID = "GHERKIN_ACTION_PARAMETER";
  public static final TextAttributesKey GHERKIN_ACTION_PARAMETER = TextAttributesKey.createTextAttributesKey(
          GHERKIN_ACTION_PARAMETER_ID,
          DefaultLanguageHighlighterColors.INSTANCE_METHOD
  );

  @NonNls
  static final String GHERKIN_DIGIT_PARAMETER_ID = "GHERKIN_DIGIT_PARAMETER";
  public static final TextAttributesKey GHERKIN_DIGIT_PARAMETER = TextAttributesKey.createTextAttributesKey(
          GHERKIN_DIGIT_PARAMETER_ID,
          DefaultLanguageHighlighterColors.NUMBER
  );

  @NonNls
  static final String GHERKIN_STRING_PARAMETER_ID = "GHERKIN_STRING_PARAMETER";
  public static final TextAttributesKey GHERKIN_STRING_PARAMETER = TextAttributesKey.createTextAttributesKey(
          GHERKIN_STRING_PARAMETER_ID,
          DefaultLanguageHighlighterColors.STRING
  );

  @NonNls
  static final String DATAJACK_PARAMETER_ID = "DATAJACK_PARAMETER";
  public static final TextAttributesKey DATAJACK_PARAMETER = TextAttributesKey.createTextAttributesKey(
          DATAJACK_PARAMETER_ID,
          DefaultLanguageHighlighterColors.CONSTANT
  );

  @NonNls
  static final String GHERKIN_TABLE_CELL_ID = "GHERKIN_TABLE_CELL";
  public static final TextAttributesKey TABLE_CELL = TextAttributesKey.createTextAttributesKey(
    GHERKIN_TABLE_CELL_ID,
    REGEXP_PARAMETER
  );

  @NonNls
  static final String GHERKIN_PYSTRING_ID = "GHERKIN_PYSTRING";
  public static final TextAttributesKey PYSTRING = TextAttributesKey.createTextAttributesKey(
    GHERKIN_PYSTRING_ID,
    DefaultLanguageHighlighterColors.STRING
  );

  public static final TextAttributesKey TEXT = TextAttributesKey.createTextAttributesKey("GHERKIN_TEXT", HighlighterColors.TEXT);

  public static final TextAttributesKey PIPE = TextAttributesKey.createTextAttributesKey("GHERKIN_TABLE_PIPE", KEYWORD);

  private GherkinHighlighter() {
  }
}

package ru.sbtqa.tag.cucumber;

import com.intellij.codeInsight.template.TemplateActionContext;
import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import org.jetbrains.annotations.NonNls;
import ru.sbtqa.tag.cucumber.psi.GherkinSyntaxHighlighter;
import ru.sbtqa.tag.cucumber.psi.PlainGherkinKeywordProvider;
import ru.sbtqa.tag.cucumber.psi.impl.GherkinFileImpl;

/**
 * @author Roman.Chernyatchik
 * @date Jun 24, 2009
 */
public class GherkinLiveTemplateContextType extends TemplateContextType {
  @NonNls
  private static final String CONTEXT_NAME = "CUCUMBER_FEATURE_FILE";

  public GherkinLiveTemplateContextType() {
    super(CONTEXT_NAME, CucumberBundle.message("live.templates.context.cucumber.name"));
  }

  @Override
  public boolean isInContext(TemplateActionContext templateActionContext) {
    return templateActionContext.getFile() instanceof  GherkinFileImpl;
  }

  @Override
  public SyntaxHighlighter createHighlighter() {
    return new GherkinSyntaxHighlighter(new PlainGherkinKeywordProvider());
  }
}

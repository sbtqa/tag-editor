package ru.sbtqa.tag.cucumber.psi.impl;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import ru.sbtqa.tag.cucumber.psi.GherkinElementVisitor;
import ru.sbtqa.tag.cucumber.psi.GherkinScenario;
import ru.sbtqa.tag.cucumber.psi.GherkinTokenTypes;

/**
 * @author yole
 */
public class GherkinScenarioImpl extends GherkinStepsHolderBase implements GherkinScenario {
  public GherkinScenarioImpl(@NotNull final ASTNode node) {
    super(node);
  }

  @Override
  public String toString() {
    if (isBackground()) {
      return "GherkinScenario(Background):";
    }
    return "GherkinScenario:" + getScenarioName();
  }

  @Override
  public boolean isBackground() {
    return getNode().getFirstChildNode().getElementType() == GherkinTokenTypes.BACKGROUND_KEYWORD;
  }

  @Override
  protected String getPresentableText() {
    return buildPresentableText(isBackground() ? "Background" : "Scenario");
  }

  @Override
  protected void acceptGherkin(GherkinElementVisitor gherkinElementVisitor) {
    gherkinElementVisitor.visitScenario(this);
  }
}

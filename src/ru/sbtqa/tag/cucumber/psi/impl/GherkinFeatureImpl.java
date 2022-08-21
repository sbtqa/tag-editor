package ru.sbtqa.tag.cucumber.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import ru.sbtqa.tag.cucumber.psi.GherkinElementVisitor;
import ru.sbtqa.tag.cucumber.psi.GherkinFeature;
import ru.sbtqa.tag.cucumber.psi.GherkinStepsHolder;
import ru.sbtqa.tag.cucumber.psi.GherkinTokenTypes;

/**
 * @author yole
 */
public class GherkinFeatureImpl extends GherkinPsiElementBase implements GherkinFeature {
  public GherkinFeatureImpl(@NotNull final ASTNode node) {
    super(node);
  }

  @Override
  public String toString() {
    return "GherkinFeature:" + getFeatureName();
  }

  @Override
  public String getFeatureName() {
    ASTNode node = getNode();
    final ASTNode firstText = node.findChildByType(GherkinTokenTypes.TEXT);
    if (firstText != null) {
      return firstText.getText();
    }
    final GherkinFeatureHeaderImpl header = PsiTreeUtil.getChildOfType(this, GherkinFeatureHeaderImpl.class);
    if (header != null) {
      return header.getElementText();
    }
    return getElementText();
  }

  @Override
  public GherkinStepsHolder[] getScenarios() {
    final GherkinStepsHolder[] children = PsiTreeUtil.getChildrenOfType(this, GherkinStepsHolder.class);
    return children == null ? GherkinStepsHolder.EMPTY_ARRAY : children;
  }

  @Override
  protected String getPresentableText() {
    return "Feature: " + getFeatureName();
  }

  @Override
  protected void acceptGherkin(GherkinElementVisitor gherkinElementVisitor) {
    gherkinElementVisitor.visitFeature(this);
  }
}

package ru.sbtqa.tag.cucumber.psi.impl;

import ru.sbtqa.tag.cucumber.psi.GherkinElementVisitor;
import org.jetbrains.annotations.NotNull;
import com.intellij.lang.ASTNode;

/**
 * @author yole
 */
public class GherkinFeatureHeaderImpl extends GherkinPsiElementBase {
  public GherkinFeatureHeaderImpl(@NotNull final ASTNode node) {
    super(node);
  }

  @Override
  protected void acceptGherkin(GherkinElementVisitor gherkinElementVisitor) {
    gherkinElementVisitor.visitFeatureHeader(this);
  }

  @Override
  public String toString() {
    return "GherkinFeatureHeader";
  }
}

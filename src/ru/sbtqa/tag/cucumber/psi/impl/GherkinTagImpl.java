package ru.sbtqa.tag.cucumber.psi.impl;

import ru.sbtqa.tag.cucumber.psi.GherkinElementVisitor;
import ru.sbtqa.tag.cucumber.psi.GherkinTag;
import org.jetbrains.annotations.NotNull;
import com.intellij.lang.ASTNode;

/**
 * @author yole
 */
public class GherkinTagImpl extends GherkinPsiElementBase implements GherkinTag {
  public GherkinTagImpl(@NotNull final ASTNode node) {
    super(node);
  }

  @Override
  protected void acceptGherkin(GherkinElementVisitor gherkinElementVisitor) {
    gherkinElementVisitor.visitTag(this);
  }

  @Override
  public String getName() {
    return getText();
  }

  @Override
  public String toString() {
    return "GherkinTag:" + getText();
  }
}

package ru.sbtqa.tag.cucumber.psi.impl;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import ru.sbtqa.tag.cucumber.psi.GherkinElementVisitor;
import ru.sbtqa.tag.cucumber.psi.GherkinPystring;

public class GherkinPystringImpl extends GherkinPsiElementBase implements GherkinPystring {
  public GherkinPystringImpl(@NotNull final ASTNode node) {
    super(node);
  }

  @Override
  protected void acceptGherkin(GherkinElementVisitor gherkinElementVisitor) {
    gherkinElementVisitor.visitPystring(this);
  }

  @Override
  public String toString() {
    return "GherkinPystring";
  }
}

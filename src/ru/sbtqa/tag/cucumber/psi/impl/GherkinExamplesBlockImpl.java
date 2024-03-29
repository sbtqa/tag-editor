package ru.sbtqa.tag.cucumber.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.sbtqa.tag.cucumber.psi.GherkinElementTypes;
import ru.sbtqa.tag.cucumber.psi.GherkinElementVisitor;
import ru.sbtqa.tag.cucumber.psi.GherkinExamplesBlock;
import ru.sbtqa.tag.cucumber.psi.GherkinTable;

/**
 * @author yole
 */
public class GherkinExamplesBlockImpl extends GherkinPsiElementBase implements GherkinExamplesBlock {
  private static final TokenSet TABLE_FILTER = TokenSet.create(GherkinElementTypes.TABLE);

  public GherkinExamplesBlockImpl(@NotNull final ASTNode node) {
    super(node);
  }

  @Override
  public String toString() {
    return "GherkinExamplesBlock:" + getElementText();
  }

  @Override
  protected String getPresentableText() {
    return buildPresentableText("Examples");
  }

  @Override
  protected void acceptGherkin(GherkinElementVisitor gherkinElementVisitor) {
    gherkinElementVisitor.visitExamplesBlock(this);
  }

  @Override
  @Nullable
  public GherkinTable getTable() {
    final ASTNode node = getNode();

    final ASTNode tableNode = node.findChildByType(TABLE_FILTER);
    return tableNode == null ? null : (GherkinTable)tableNode.getPsi();
  }
}

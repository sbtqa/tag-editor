package ru.sbtqa.tag.spellchecker;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import org.jetbrains.annotations.NotNull;
import ru.sbtqa.tag.cucumber.psi.GherkinElementType;

/**
 * @author oleg
 */
public class GherkinSpellcheckerStrategy extends SpellcheckingStrategy {
  @NotNull
  @Override
  public Tokenizer getTokenizer(final PsiElement element) {
    if (element instanceof LeafElement) {
      final ASTNode node = element.getNode();
      if (node != null && node.getElementType() instanceof GherkinElementType){
        return SpellcheckingStrategy.TEXT_TOKENIZER;
      }
    }
    return super.getTokenizer(element);
  }

}
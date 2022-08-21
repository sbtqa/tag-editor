package ru.sbtqa.tag.cucumber.psi;

/**
 * @author yole
 */
public interface GherkinExamplesBlock extends GherkinPsiElement {
  GherkinTable getTable();
}

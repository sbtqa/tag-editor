package ru.sbtqa.tag.cucumber.psi;

import org.jetbrains.annotations.NotNull;

/**
 * @author Roman.Chernyatchik
 * @date Aug 22, 2009
 */
public interface GherkinStepsHolder extends GherkinPsiElement, GherkinSuppressionHolder {
  GherkinStepsHolder[] EMPTY_ARRAY = new GherkinStepsHolder[0];

  String getScenarioName();

  @NotNull
  GherkinStep[] getSteps();

  GherkinTag[] getTags();
}

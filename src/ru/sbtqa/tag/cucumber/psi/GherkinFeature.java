package ru.sbtqa.tag.cucumber.psi;

/**
 * @author yole
 */
public interface GherkinFeature extends GherkinPsiElement, GherkinSuppressionHolder {
  String getFeatureName();
  GherkinStepsHolder[] getScenarios();
}

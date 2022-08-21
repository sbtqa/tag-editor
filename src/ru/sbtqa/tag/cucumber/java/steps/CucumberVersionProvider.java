package ru.sbtqa.tag.cucumber.java.steps;

import ru.sbtqa.tag.cucumber.java.config.CucumberConfigUtil;
import ru.sbtqa.tag.cucumber.psi.GherkinStep;

public class CucumberVersionProvider {

    public String getVersion(GherkinStep step) {
        return CucumberConfigUtil.getCucumberCoreVersion(step);
    }
}
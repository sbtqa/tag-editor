package ru.sbtqa.tag.cucumber.java.steps;

import ru.sbtqa.tag.cucumber.java.config.CucumberConfigUtil;
import ru.sbtqa.tag.cucumber.psi.GherkinFile;
import ru.sbtqa.tag.cucumber.psi.GherkinStep;

import static java.lang.String.format;

public class AnnotationPackageProvider {

    private static final String CUCUMBER_1_1_ANNOTATION_BASE_PACKAGE = "cucumber.api.java";
    private static final String CUCUMBER_1_0_ANNOTATION_BASE_PACKAGE = "cucumber.annotation";
    private final CucumberVersionProvider myVersionProvider;

    public AnnotationPackageProvider() {
        this(new CucumberVersionProvider());
    }

    public AnnotationPackageProvider(CucumberVersionProvider cucumberVersionProvider) {
        myVersionProvider = cucumberVersionProvider;
    }

    private static String locale(GherkinStep step) {
        GherkinFile file = (GherkinFile) step.getContainingFile();
        return file.getLocaleLanguage().replaceAll("-", "_");
    }

    public String getAnnotationPackageFor(GherkinStep step) {
        return format("%s.%s", annotationBasePackage(step), locale(step));
    }

    private String annotationBasePackage(GherkinStep step) {
        final String version = myVersionProvider.getVersion(step);
        if (version != null && version.compareTo(CucumberConfigUtil.CUCUMBER_VERSION_1_1) < 0) {
            return CUCUMBER_1_0_ANNOTATION_BASE_PACKAGE;
        }
        return CUCUMBER_1_1_ANNOTATION_BASE_PACKAGE;
    }
}
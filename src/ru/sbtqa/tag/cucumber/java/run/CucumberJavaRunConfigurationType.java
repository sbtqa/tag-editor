// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package ru.sbtqa.tag.cucumber.java.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.components.BaseState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyValue;
import icons.CucumberJavaIcons;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

public final class CucumberJavaRunConfigurationType extends ConfigurationTypeBase {

    public CucumberJavaRunConfigurationType() {
        super("CucumberJavaRunConfigurationType", "Cucumber java", null,
                NotNullLazyValue.createValue(() -> CucumberJavaIcons.CucumberJavaRunConfiguration));
        addFactory(new ConfigurationFactory(this) {
            @Override
            public Icon getIcon() {
                return CucumberJavaRunConfigurationType.this.getIcon();
            }

            public String getId() { return CucumberJavaRunConfigurationType.this.getId(); }

            @Override
            @NotNull
            public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
                return new CucumberJavaRunConfiguration(getDisplayName(), project, this);
            }

            @Override
            public Class<? extends BaseState> getOptionsClass() {
                return CucumberJavaConfigurationOptions.class;
            }
        });
    }

    @NotNull
    public static CucumberJavaRunConfigurationType getInstance() {
        return ConfigurationTypeUtil.findConfigurationType(CucumberJavaRunConfigurationType.class);
    }

    @Override
    public String getHelpTopic() {
        return "reference.dialogs.rundebug.CucumberJavaRunConfigurationType";
    }
}

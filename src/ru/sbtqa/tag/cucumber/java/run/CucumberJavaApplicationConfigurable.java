package ru.sbtqa.tag.cucumber.java.run;

import com.intellij.application.options.ModuleDescriptionsComboBox;
import com.intellij.execution.configurations.ConfigurationUtil;
import com.intellij.execution.ui.ClassBrowser;
import com.intellij.execution.ui.CommonJavaParametersPanel;
import com.intellij.execution.ui.ConfigurationModuleSelector;
import com.intellij.execution.ui.DefaultJreSelector;
import com.intellij.execution.ui.JrePathEditor;
import com.intellij.execution.ui.ShortenCommandLineModeCombo;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaCodeFragment;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiMethodUtil;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.EditorTextFieldWithBrowseButton;
import com.intellij.ui.PanelWithAnchor;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.util.ui.UIUtil;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CucumberJavaApplicationConfigurable extends SettingsEditor<CucumberJavaRunConfiguration> implements PanelWithAnchor {

    private final Project myProject;
    private final ConfigurationModuleSelector myModuleSelector;
    private final Module myModuleContext;
    private JComponent myAnchor;
    private LabeledComponent<ComponentWithBrowseButton<EditorTextField>> myMainClass;
    private JPanel myWholePanel;
    private LabeledComponent<ModuleDescriptionsComboBox> myModule;
    private LabeledComponent<RawCommandLineEditor> myGlue;
    private LabeledComponent<TextFieldWithBrowseButton> myFeatureOrFolder;
    private CommonJavaParametersPanel myCommonProgramParameters;
    private LabeledComponent<ShortenCommandLineModeCombo> myShortenClasspathModeCombo;
    private JrePathEditor myJrePathEditor;

    public CucumberJavaApplicationConfigurable(Project project) {
        myProject = project;
        ModuleDescriptionsComboBox moduleComponent = myModule.getComponent();
        myModuleSelector = new ConfigurationModuleSelector(project, moduleComponent);
        myJrePathEditor.setDefaultJreSelector(DefaultJreSelector
                .fromModuleDependencies(moduleComponent, false));

        ClassBrowser.AppClassBrowser<EditorTextField> appClassBrowser = new ClassBrowser
                .AppClassBrowser.AppClassBrowser<EditorTextField>(project, myModuleSelector);
        appClassBrowser.setField(myMainClass.getComponent());

        myModuleContext = myModuleSelector.getModule();


        final ActionListener fileToRunActionListener = actionEvent -> {
            FileChooserDescriptor fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor();
            fileChooserDescriptor.setTitle("Choose Feature File or Directory");
            fileChooserDescriptor.putUserData(LangDataKeys.MODULE_CONTEXT, myModuleContext);
            VirtualFile file = FileChooser.chooseFile(fileChooserDescriptor, myProject, null);
            if (file != null) {
                setFeatureOrFolder(file.getPresentableUrl());
            }
        };
        myFeatureOrFolder.getComponent().addActionListener(fileToRunActionListener);

        myAnchor = UIUtil.mergeComponentsWithAnchor(myMainClass, myGlue, myFeatureOrFolder, myModule, myCommonProgramParameters);

        myJrePathEditor.setAnchor(myModule.getLabel());
        myShortenClasspathModeCombo.setAnchor(myModule.getLabel());
        myShortenClasspathModeCombo.setComponent(new ShortenCommandLineModeCombo(myProject, myJrePathEditor, moduleComponent));

        myGlue.getComponent().setText("Glue");
    }

    public void setFeatureOrFolder(String path) {
        myFeatureOrFolder.getComponent().setText(path);
    }

    private void createUIComponents() {
        myMainClass = new LabeledComponent<>();
        myMainClass.setComponent(new EditorTextFieldWithBrowseButton(myProject, true, new JavaCodeFragment.VisibilityChecker() {
            @Override
            public Visibility isDeclarationVisible(PsiElement declaration, PsiElement place) {
                if (declaration instanceof PsiClass) {
                    final PsiClass aClass = (PsiClass) declaration;
                    if (ConfigurationUtil.MAIN_CLASS.value(aClass) && PsiMethodUtil.findMainMethod(aClass) != null) {
                        return Visibility.VISIBLE;
                    }
                }
                return Visibility.NOT_VISIBLE;
            }
        }));
    }

    @Override
    public JComponent getAnchor() {
        return myAnchor;
    }

    @Override
    public void setAnchor(@Nullable JComponent anchor) {
        myAnchor = anchor;
        myMainClass.setAnchor(anchor);
        myGlue.setAnchor(anchor);
        myFeatureOrFolder.setAnchor(anchor);
        myModule.setAnchor(anchor);
        myCommonProgramParameters.setAnchor(anchor);
    }

    @Override
    protected void resetEditorFrom(@NotNull CucumberJavaRunConfiguration configuration) {
        myModuleSelector.reset(configuration);
        myCommonProgramParameters.reset(configuration);

        myMainClass.getComponent().getChildComponent().setText(configuration.getMainClassName());
        myGlue.getComponent().setText(configuration.getGlue());
        myFeatureOrFolder.getComponent().setText(configuration.getFilePath());
        myJrePathEditor.setPathOrName(configuration.getAlternativeJrePath(), configuration.isAlternativeJrePathEnabled());
        myShortenClasspathModeCombo.getComponent().setSelectedItem(configuration.getShortenCommandLine());
    }

    @Override
    protected void applyEditorTo(@NotNull CucumberJavaRunConfiguration configuration) {
        myCommonProgramParameters.applyTo(configuration);
        myModuleSelector.applyTo(configuration);

        configuration.setMainClassName(myMainClass.getComponent().getChildComponent().getText());
        configuration.setGlue(myGlue.getComponent().getText());
        configuration.setFilePath(myFeatureOrFolder.getComponent().getText());
        configuration.setAlternativeJrePath(myJrePathEditor.getJrePathOrName());
        configuration.setAlternativeJrePathEnabled(myJrePathEditor.isAlternativeJreSelected());
        configuration.setShortenCommandLine(myShortenClasspathModeCombo.getComponent().getSelectedItem());
        configuration.setModule(myModuleSelector.getModule());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return myWholePanel;
    }
}

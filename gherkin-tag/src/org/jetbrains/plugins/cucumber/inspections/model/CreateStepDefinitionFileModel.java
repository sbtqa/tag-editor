package org.jetbrains.plugins.cucumber.inspections.model;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.BDDFrameworkType;

public class CreateStepDefinitionFileModel {

    private final Map<BDDFrameworkType, PsiDirectory> myFileTypeToDefaultDirectoryMap;
    private final Project myProject;
    DefaultComboBoxModel myFileTypeModel;
    private String myFileName;
    private PsiDirectory myDirectory;

    public CreateStepDefinitionFileModel(@NotNull final Project project, @NotNull final Map<BDDFrameworkType, String> fileTypeToDefaultNameMap, @NotNull final Map<BDDFrameworkType, PsiDirectory> fileTypeToDefaultDirectoryMap) {
        myProject = project;
        List<FileTypeComboboxItem> myFileTypeList = new ArrayList<>();
        for (Map.Entry<BDDFrameworkType, String> entry : fileTypeToDefaultNameMap.entrySet()) {
            if (myFileName == null) {
                myFileName = entry.getValue();
            }
            FileTypeComboboxItem item = new FileTypeComboboxItem(entry.getKey(), entry.getValue());
            myFileTypeList.add(item);
        }
        myFileTypeToDefaultDirectoryMap = fileTypeToDefaultDirectoryMap;
        myFileTypeModel = new DefaultComboBoxModel(myFileTypeList.toArray());
        myDirectory = fileTypeToDefaultDirectoryMap.get(getSelectedFileType());
    }

    public String getFilePath() {
        final StringBuilder result = new StringBuilder();
        result.append(getDirectory().getVirtualFile().getPath()).append(File.separator).append(getFileNameWithExtension());
        return result.toString();
    }

    public String getFileNameWithExtension() {
        final StringBuilder result = new StringBuilder();
        result.append(myFileName)
                .append('.').append(getSelectedFileType().getFileType().getDefaultExtension());
        return result.toString();
    }

    public String getFileName() {
        return myFileName;
    }

    public void setFileName(@NotNull final String fileName) {
        myFileName = fileName;
    }

    public PsiDirectory getDefaultDirectory() {
        return myFileTypeToDefaultDirectoryMap.get(getSelectedFileType());
    }

    public PsiDirectory getDirectory() {
        return myDirectory;
    }

    public void setDirectory(@Nullable final PsiDirectory psiDirectory) {
        myDirectory = psiDirectory;
    }

    public BDDFrameworkType getSelectedFileType() {
        final FileTypeComboboxItem selectedItem = (FileTypeComboboxItem) myFileTypeModel.getSelectedItem();
        return selectedItem != null ? selectedItem.getFrameworkType() : null;
    }

    public DefaultComboBoxModel getFileTypeModel() {
        return myFileTypeModel;
    }

    public Project getProject() {
        return myProject;
    }
}

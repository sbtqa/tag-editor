package ru.sbtqa.tag.cucumber.inspections

import com.intellij.psi.PsiFile
import ru.sbtqa.tag.cucumber.BDDFrameworkType

data class CucumberStepDefinitionCreationContext(var psiFile: PsiFile? = null, var frameworkType: BDDFrameworkType? = null)
package ru.sbtqa.tag.cucumber.java.steps;

import com.intellij.codeInsight.CodeInsightUtilCore;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.lang.Language;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.JVMElementFactories;
import com.intellij.psi.JVMElementFactory;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiLambdaExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiReferenceList;
import com.intellij.psi.TokenType;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.impl.source.tree.Factory;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import cucumber.runtime.snippets.CamelCaseConcatenator;
import cucumber.runtime.snippets.FunctionNameGenerator;
import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Step;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;
import ru.sbtqa.tag.cucumber.psi.GherkinStep;

public class Java8StepDefinitionCreator extends JavaStepDefinitionCreator {

    public static final String CUCUMBER_API_JAVA8_EN = "cucumber.api.java8.En";
    private static final String FILE_TEMPLATE_CUCUMBER_JAVA_8_STEP_DEFINITION_JAVA = "Cucumber Java 8 Step Definition.java";

    private static PsiMethod getConstructor(PsiClass clazz) {
        if (clazz.getConstructors().length == 0) {
            JVMElementFactory factory = JVMElementFactories.requireFactory(clazz.getLanguage(), clazz.getProject());
            PsiMethod constructor = factory.createConstructor(clazz.getName());
            return (PsiMethod) clazz.add(constructor);
        }
        return clazz.getConstructors()[0];
    }

    private static PsiElement buildStepDefinitionByStep(@NotNull final GherkinStep step, Language language) {
        final Step cucumberStep = new Step(new ArrayList<>(), step.getKeyword().getText(), step.getStepName(), 0, null, null);
        final SnippetGenerator generator = new SnippetGenerator(new Java8Snippet());

        String snippetTemplate = generator.getSnippet(cucumberStep, new FunctionNameGenerator(new CamelCaseConcatenator()));
        String snippet = escapeStepDefinition(snippetTemplate, step);

        JVMElementFactory factory = JVMElementFactories.requireFactory(language, step.getProject());
        PsiElement expression = factory.createExpressionFromText(snippet, step);

        try {
            return createStepDefinitionFromSnippet(expression, step, factory);
        } catch (Exception e) {
            return expression;
        }
    }

    private static PsiElement createStepDefinitionFromSnippet(@NotNull PsiElement snippetExpression, @NotNull GherkinStep step,
                                                              @NotNull JVMElementFactory factory) {
        PsiMethodCallExpression callExpression = (PsiMethodCallExpression) snippetExpression;
        PsiExpression[] arguments = callExpression.getArgumentList().getExpressions();
        PsiLambdaExpression lambda = (PsiLambdaExpression) arguments[1];

        FileTemplateDescriptor fileTemplateDescriptor = new FileTemplateDescriptor(FILE_TEMPLATE_CUCUMBER_JAVA_8_STEP_DEFINITION_JAVA);
        FileTemplate fileTemplate = FileTemplateManager.getInstance(snippetExpression.getProject()).getCodeTemplate(fileTemplateDescriptor.getFileName());
        String text = fileTemplate.getText().replace("${STEP_KEYWORD}", callExpression.getMethodExpression().getText())
                .replace("${STEP_REGEXP}", arguments[0].getText())
                .replace("${PARAMETERS}", lambda.getParameterList().getText())
                .replace("${BODY}\n", "");

        text = escapeStepDefinition(text, step);

        return factory.createExpressionFromText(text, step);
    }

    @NotNull
    @Override
    public PsiFile createStepDefinitionContainer(@NotNull PsiDirectory dir, @NotNull String name) {
        final PsiFile result = super.createStepDefinitionContainer(dir, name);

        final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(dir.getProject()).getFileIndex();
        final Module module = fileIndex.getModuleForFile(result.getVirtualFile());
        assert module != null;
        final GlobalSearchScope dependenciesScope = module.getModuleWithDependenciesAndLibrariesScope(true);

        final PsiClass stepDefContainerInterface =
                JavaPsiFacade.getInstance(module.getProject()).findClass(CUCUMBER_API_JAVA8_EN, dependenciesScope);

        if (stepDefContainerInterface != null) {
            final PsiClass createPsiClass = PsiTreeUtil.getChildOfType(result, PsiClass.class);
            assert createPsiClass != null;
            final PsiElementFactory elementFactory = JavaPsiFacade.getInstance(dir.getProject()).getElementFactory();
            PsiJavaCodeReferenceElement ref = elementFactory.createClassReferenceElement(stepDefContainerInterface);
            if (stepDefContainerInterface.isInterface()) {
                PsiReferenceList implementsList = createPsiClass.getImplementsList();
                if (implementsList != null) {
                    WriteAction.run(() -> implementsList.add(ref));
                }
            }
        }

        return result;
    }

    @NotNull
    @Override
    public String getStepDefinitionFilePath(@NotNull PsiFile file) {
        return super.getStepDefinitionFilePath(file) + " (Java 8 style)";
    }

    @Override
    public boolean createStepDefinition(@NotNull GherkinStep step, @NotNull PsiFile file) {
        if (!(file instanceof PsiClassOwner)) return false;

        final PsiClass clazz = PsiTreeUtil.getChildOfType(file, PsiClass.class);
        if (clazz == null) {
            return false;
        }

        final Project project = file.getProject();
        closeActiveTemplateBuilders(file);
        PsiDocumentManager.getInstance(project).commitAllDocuments();

        final PsiElement stepDef = buildStepDefinitionByStep(step, file.getLanguage());

        final PsiMethod constructor = getConstructor(clazz);
        final PsiCodeBlock constructorBody = constructor.getBody();
        if (constructorBody == null) {
            return false;
        }

        PsiElement anchor = constructorBody.getFirstChild();
        if (constructorBody.getStatements().length > 0) {
            anchor = constructorBody.getStatements()[constructorBody.getStatements().length - 1];
        }
        PsiElement addedStepDef = constructorBody.addAfter(stepDef, anchor);
        wrapStepDefWithLineBreakAndSemicolon(addedStepDef);

        addedStepDef = CodeInsightUtilCore.forcePsiPostprocessAndRestoreElement(addedStepDef);

        JavaCodeStyleManager.getInstance(project).shortenClassReferences(addedStepDef);

        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        assert editor != null;

        if (!(addedStepDef instanceof PsiMethodCallExpression)) {
            return false;
        }
        PsiMethodCallExpression stepDefCall = (PsiMethodCallExpression) addedStepDef;
        if (stepDefCall.getArgumentList().getExpressions().length < 2) {
            return false;
        }

        final PsiExpression regexpElement = stepDefCall.getArgumentList().getExpressions()[0];

        final PsiExpression secondArgument = stepDefCall.getArgumentList().getExpressions()[1];
        if (!(secondArgument instanceof PsiLambdaExpression)) {
            return false;
        }
        PsiLambdaExpression lambda = (PsiLambdaExpression) secondArgument;
        final PsiParameterList blockVars = lambda.getParameterList();
        PsiElement lambdaBody = lambda.getBody();
        if (!(lambdaBody instanceof PsiCodeBlock)) {
            return false;
        }
        final PsiCodeBlock body = (PsiCodeBlock) lambdaBody;

        runTemplateBuilderOnAddedStep(editor, addedStepDef, regexpElement, blockVars, body);

        return true;
    }

    protected void wrapStepDefWithLineBreakAndSemicolon(PsiElement addedStepDef) {
        LeafElement linebreak = Factory.createSingleLeafElement(TokenType.WHITE_SPACE, "\n", 0, 1, null, addedStepDef.getManager());
        addedStepDef.getParent().addBefore(linebreak.getPsi(), addedStepDef);

        LeafElement semicolon = Factory.createSingleLeafElement(JavaTokenType.SEMICOLON, ";", 0, 1, null, addedStepDef.getManager());
        addedStepDef.getParent().addAfter(semicolon.getPsi(), addedStepDef);
    }
}

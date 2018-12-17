package org.jetbrains.plugins.cucumber.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PlatformPatterns;

/**
 * Created by SBT-Tatciy-IO on 19.07.2017.
 */
public class PageEntryCompletionContributor extends CompletionContributor {

    public PageEntryCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new PageEntryCompletionProvider());
    }
}

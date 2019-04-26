package org.jetbrains.plugins.cucumber.completion;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import ru.sbtqa.tag.editor.idea.utils.StringUtils;

public class TagCompletionElement implements Comparable<TagCompletionElement> {

    private String presentableText;
    private String typeText;

    public TagCompletionElement(String presentableText, String typeText) {
        this.presentableText = StringUtils.unquote(presentableText);
        this.typeText = typeText;
    }

    public String getPresentableText() {
        return presentableText;
    }

    public String getTypeText() {
        return typeText;
    }

    @Override
    public int compareTo(@NotNull TagCompletionElement other) {
        String thisType = this.getTypeText();
        String otherType = other.getTypeText();

        int compare = thisType.compareTo(otherType);
        if (compare != 0) {
            return compare;
        }

        String thisPresentable = this.getPresentableText();
        String otherPresentable = other.getPresentableText();
        return thisPresentable.compareTo(otherPresentable);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if (!(object instanceof TagCompletionElement)) {
            return false;
        }

        TagCompletionElement tagCompletionElement = (TagCompletionElement) object;

        return this.getTypeText().equals(tagCompletionElement.getTypeText())
                && this.getPresentableText().equals(tagCompletionElement.getPresentableText());
    }

    @Override
    public int hashCode() {
        return Objects.hash(presentableText, typeText);
    }
}

package ru.sbtqa.tag.editor.idea.utils;

public class StringUtils extends org.apache.commons.lang3.StringUtils {

    private StringUtils() {}

    public static String unquote(String string) {
        if (string.startsWith("\"") && string.endsWith("\"")) {
            return string.substring(1, string.length() - 1);
        }

        return string;
    }
}
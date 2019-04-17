package ru.sbtqa.tag.editor.idea.utils;

public class StringUtils {

    private StringUtils() {}

    public static String unquote(String string) {
        if (string.charAt(0) == '\"'
                && string.charAt(string.length() - 1) == '\"') {
            return string.substring(1, string.length() - 1);
        }

        return string;
    }
}

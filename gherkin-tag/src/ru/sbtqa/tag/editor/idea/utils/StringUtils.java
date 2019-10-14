package ru.sbtqa.tag.editor.idea.utils;

public class StringUtils extends org.apache.commons.lang3.StringUtils {

    public static final String QUOTE = "\"";
    public static final String EMPTY_STRING = "";
    public static final String NON_CRITICAL = "^(\\s*\\?\\s*)*";


    private StringUtils() {}

    public static String unquote(String string) {
        while(string.startsWith(QUOTE) && string.endsWith(QUOTE)) {
            string = string.substring(1, string.length() - 1);
        }
        return string;
    }
}
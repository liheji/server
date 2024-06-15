package top.yilee.server;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.nio.charset.StandardCharsets;


public class DecodeCookie {

    public static void main(String[] args) {
        String[] res = decodeCookie("ZjVEUDRwUUZNalQzWHg1NGlHdTRUdyUzRCUzRDpYRklDZ3hvMlVLd05VZ0d6UyUyRnluT1ElM0QlM0Q");
        if (res != null) {
            System.out.println(Arrays.toString(res));
        }
    }

    public static String[] decodeCookie(String cookieValue) {
        for (int j = 0; j < cookieValue.length() % 4; j++) {
            cookieValue = cookieValue + "=";
        }
        String cookieAsPlainText;
        try {
            cookieAsPlainText = new String(Base64.getDecoder().decode(cookieValue.getBytes()));
        } catch (IllegalArgumentException ex) {
            return null;
        }
        String[] tokens = delimitedListToStringArray(cookieAsPlainText, ":");
        for (int i = 0; i < tokens.length; i++) {
            try {
                tokens[i] = URLDecoder.decode(tokens[i], StandardCharsets.UTF_8.toString());
            } catch (UnsupportedEncodingException ex) {
                return null;
            }
        }
        return tokens;
    }


    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final String FOLDER_SEPARATOR = "/";
    private static final char FOLDER_SEPARATOR_CHAR = '/';
    private static final String WINDOWS_FOLDER_SEPARATOR = "\\";
    private static final String TOP_PATH = "..";
    private static final String CURRENT_PATH = ".";
    private static final char EXTENSION_SEPARATOR = '.';
    private static final int DEFAULT_TRUNCATION_THRESHOLD = 100;
    private static final String TRUNCATION_SUFFIX = " (truncated)...";

    public static String[] delimitedListToStringArray(String str, String delimiter) {
        return delimitedListToStringArray(str, delimiter, null);
    }

    public static String[] delimitedListToStringArray(String str, String delimiter, String charsToDelete) {
        if (str == null) {
            return EMPTY_STRING_ARRAY;
        } else if (delimiter == null) {
            return new String[]{str};
        } else {
            List<String> result = new ArrayList<>();
            int pos;
            if (delimiter.isEmpty()) {
                for (pos = 0; pos < str.length(); ++pos) {
                    result.add(deleteAny(str.substring(pos, pos + 1), charsToDelete));
                }
            } else {
                int delPos;
                for (pos = 0; (delPos = str.indexOf(delimiter, pos)) != -1; pos = delPos + delimiter.length()) {
                    result.add(deleteAny(str.substring(pos, delPos), charsToDelete));
                }

                if (!str.isEmpty() && pos <= str.length()) {
                    result.add(deleteAny(str.substring(pos), charsToDelete));
                }
            }

            return toStringArray(result);
        }
    }

    public static String deleteAny(String inString, String charsToDelete) {
        if (hasLength(inString) && hasLength(charsToDelete)) {
            int lastCharIndex = 0;
            char[] result = new char[inString.length()];

            for (int i = 0; i < inString.length(); ++i) {
                char c = inString.charAt(i);
                if (charsToDelete.indexOf(c) == -1) {
                    result[lastCharIndex++] = c;
                }
            }

            if (lastCharIndex == inString.length()) {
                return inString;
            } else {
                return new String(result, 0, lastCharIndex);
            }
        } else {
            return inString;
        }
    }


    public static boolean hasLength(String str) {
        return str != null && !str.isEmpty();
    }

    public static String[] toStringArray(Collection<String> collection) {
        return !isEmpty(collection) ? collection.toArray(EMPTY_STRING_ARRAY) : EMPTY_STRING_ARRAY;
    }

    public static String[] toStringArray(Enumeration<String> enumeration) {
        return enumeration != null ? toStringArray(Collections.list(enumeration)) : EMPTY_STRING_ARRAY;
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
}

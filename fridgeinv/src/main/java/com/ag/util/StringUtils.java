package com.ag.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class StringUtils {

    private static final int EOF = -1;

    public static class ReaderState {
        public int character;
        public Object state;
    }

    /**
     * Parses elements at each unquoted comma. The quote character (" or ') can be
     * escaped
     * with the backslash '\' character when in a quoted field. A field is
     * considered quoted when
     * the first character of a field (at index 0 or immediately after an unquoted
     * comma) is a single
     * or double quote character.
     * 
     * @param listString
     * @return the List of String elements
     */
    public static List<String> parseCommaDelimitedList(String listString) {
        return parseDelimitedList(listString, ',');
    }

    public static String parseToNextUnescapedDelimiter(Reader reader, final int delimiter, final int escape) throws IOException {
        return parseStringUntil(reader, state -> {
            Boolean isEscaped = null == state.state ? false : (Boolean) state.state;
            state.state = state.character == escape && !isEscaped;
            return !isEscaped && state.character == delimiter;
        });
    }

    public static String parseToNextUnescapedDelimiter(Reader reader, final int delimiter) throws IOException {
        return parseToNextUnescapedDelimiter(reader, delimiter, '\\');
    }

    public static String parseToNextUnescapedDelimiter(String string, final int delimiter, final int escape) throws IOException {
        return parseToNextUnescapedDelimiter(new StringReader(string), delimiter, escape);
    }

    public static String parseToNextUnescapedDelimiter(String string, final int delimiter) throws IOException {
        return parseToNextUnescapedDelimiter(string, delimiter, '\\');
    }

    public static String parseToWhitespace(Reader reader) throws IOException {
        return parseStringUntil(reader, state -> Character.isWhitespace(state.character));
    }

    public static String parseToWhitespace(String string) throws IOException {
        return parseToWhitespace(new StringReader(string));
    }

    public static String parseToWhitespaceOrDelimiter(Reader reader, Set<Integer> delimiters) throws IOException {
        return parseStringUntil(reader, state -> {
            return Character.isWhitespace(state.character) 
                || delimiters.contains(state.character);
        });
    }

    public static String parseToWhitespaceOrDelimiter(Reader reader, int delimiter) throws IOException {
        HashSet<Integer> delims = new HashSet<>();
        delims.add(delimiter);
        return parseToWhitespaceOrDelimiter(reader, delims);
    }

    public static String parseStringUntil(Reader reader, Predicate<ReaderState> condition)
            throws IOException {

        if (!reader.markSupported()) {
            throw new IllegalArgumentException("Reader must support mark()");
        }

        StringBuilder sb = new StringBuilder();
        ReaderState state = new ReaderState();
        while (isNotEOFAndNotCondition(reader, condition, state)) {
            // Not a great assumption, only supports ascii
            sb.append((char) reader.read());
        }
        return sb.toString();
    }

    private static boolean isNotEOFAndNotCondition(Reader reader, Predicate<ReaderState> condition, ReaderState state) throws IOException {
        int c = peek(reader);
        state.character = c;
        return c > EOF && !condition.test(state);
    }

    private static int peek(Reader reader) throws IOException {
        reader.mark(1);
        int c = reader.read();
        reader.reset();
        return c;
    }

    /**
     * Parses elements at each unquoted delimiter. The quote character (" or ') can
     * be escaped
     * with the backslash '\' character when in a quoted field. A field is
     * considered quoted when
     * the first character of a field (at index 0 or immediately after an unquoted
     * comma) is a single
     * or double quote character.
     * 
     * @param listString
     * @return the List of String elements
     */
    public static List<String> parseDelimitedList(String listString, final char delimiter) {
        List<String> elements = new ArrayList<>();
        char[] chars = listString.toCharArray();
        StringBuilder sb = new StringBuilder();
        int quoteStartIndex = 0;
        char lastChar = '\0';
        char quoteChar = '"';
        boolean isQuoted = false;
        boolean isEscaped = false;

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            if (delimiter == c) {
                if (isQuoted) {
                    sb.append(c);
                } else {
                    // add string and reset buffer
                    elements.add(sb.toString());
                    sb.setLength(0);
                }
            } else if ('\\' == c) {
                if (isEscaped) {
                    sb.append(c);
                }
                isEscaped = !isEscaped;
            } else if ('"' == c || '\'' == c) {
                if (isEscaped) {
                    sb.append(c);
                } else {
                    if (isQuoted && c == quoteChar) {
                        isQuoted = false;
                        // all quoted fields must quote the entire field
                        if (chars.length - 1 != i && ',' != chars[i + 1]) {
                            throw new RuntimeException("Quote does not take up entire field. Started at index "
                                    + quoteStartIndex + " to index " + i);
                        }
                    } else {
                        // we are only in quoted state when a quote
                        // starts immediately after comma.
                        if (',' == lastChar || 0 == i) {
                            quoteChar = c;
                            isQuoted = true;
                            quoteStartIndex = i;
                        } else {
                            sb.append(c);
                        }
                    }
                }
            } else {
                sb.append(c);
            }

            // reset isEscaped after first non-escape char
            if ('\\' != c) {
                isEscaped = false;
            }
            lastChar = c;
        }

        if (isQuoted) {
            throw new IllegalArgumentException("Unmatched quote at index " + quoteStartIndex);
        } else if (isEscaped) {
            throw new IllegalArgumentException("Unmatched terminal escape character");
        }

        // add final element
        elements.add(sb.toString());

        return elements;
    }

    public static boolean isEmptyOrNull(String string) {
        return null == string || string.isEmpty();
    }

    public static boolean isBlankOrNull(String string) {
        return null == string || string.isBlank();
    }

    public static String quote(String s) {
        return wrapWith(s, '"');
    }

    public static String wrapWith(String s, char c) {
        return c + s + c;
    }

}

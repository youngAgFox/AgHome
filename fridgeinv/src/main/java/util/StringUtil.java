package util;
import java.util.ArrayList;
import java.util.List;

public class StringUtil {
    
        /**
         * Parses elements at each unquoted comma. The quote character (" or ') can be escaped 
         * with the backslash '\' character when in a quoted field. A field is considered quoted when
         * the first character of a field (at index 0 or immediately after an unquoted comma) is a single
         * or double quote character.
         * 
         * @param listString
         * @return the List of String elements
         */
        public static List<String> parseCommaDelimitedList(String listString) {
            return parseDelimitedList(listString, ',');
        }

        /**
         * Parses elements at each unquoted delimiter. The quote character (" or ') can be escaped 
         * with the backslash '\' character when in a quoted field. A field is considered quoted when
         * the first character of a field (at index 0 or immediately after an unquoted comma) is a single
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

        public boolean isEmptyOrNull(String string) {
            return null == string || string.isEmpty();
        }

        public boolean isBlankOrNull(String string) {
            return null == string || string.isBlank();
        }

}

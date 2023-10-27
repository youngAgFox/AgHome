package com.ag.json;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.regex.Pattern;

import util.StringUtils;

public class JsonTokenReader implements Iterator<JsonToken> {

    private static final int EOF = -1;

    // matches numbers (incl. just zero) with decimal and/or exponents and does not allow leading zeros
    private static final Pattern numberLiteralRegex = Pattern.compile("^0$|^-?[1-9]\\d*(?:\\.\\d+)?(?:e[1-9]\\d*)?$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern boolLiteralRegex = Pattern.compile("^true$|^false$", Pattern.CASE_INSENSITIVE);
    private static final Pattern nullLiteralRegex = Pattern.compile("^null$", Pattern.CASE_INSENSITIVE);

    private final Reader reader;

    private int col = 0;
    private int lastCol;
    private int row = 1;

    public JsonTokenReader(Reader reader) {
        this.reader = reader;

        if (null == reader || !this.reader.markSupported()) {
            throw new IllegalArgumentException("JsonReader requires a non-null reader that supports mark()");
        }
    }

    private int peek() throws IOException {
        // don't inc / dec here as we immediately reset
        reader.mark(1);
        int c = reader.read();
        reader.reset();
        return c;
    }

    @Override
    public boolean hasNext() {
        try {
            skipWhitespace();
            return peek() > EOF;
        } catch (IOException e) {
            throw new JsonParseException("hasNext() failed", e);
        }
    }

    @Override
    public JsonToken next() {
        int c, startCol, startRow;
        try {
            skipWhitespace();
            c = readAndIncCursor();
            startCol = col;
            startRow = row;
            JsonToken token = parseToken(c);
            token.cursor = new JsonParserCursor(startCol, startRow);
            return token;
        } catch (IOException e) {
            throw new JsonParseException("Failed to parse next token at (" + row + ", " + col + ")", e);
        }
    }

    private void skipWhitespace() throws IOException {
        int c;
        do {
            reader.mark(1);
            c = readAndIncCursor();
        } while (c > EOF && Character.isWhitespace(c));

        // put the read non-whitespace character back into stream
        if (c > EOF) {
            resetAndDecCursor(c);
        }
    }

    private JsonToken parseToken(int c) throws IOException {
        if (c <= EOF) {
            throw new JsonParseException("Hit end of tokens while parsing is incomplete");
        }

        JsonTokenType tokenType = JsonToken.JSON_TOKENS.get(c);
        if (JsonTokenType.STRING == tokenType) {
            return parseJsonString(reader, c);
        } else if (null != tokenType) {
            return new JsonToken(tokenType, c);
        }
        // literals do not have a set start char, so the retrieved token type will be null
        return parseJsonLiteral(reader, c);
    }

    private JsonToken parseJsonString(Reader reader, int c) throws IOException {
        int startCol = col;
        int startRow = row;
        JsonToken token = new JsonToken(JsonTokenType.STRING,
                StringUtils.parseToNextUnescapedDelimiter(reader, JsonTokenType.STRING.startChar));
        incCursor(token.value);
        // consume the last quote
        int trailingQuote = readAndIncCursor();
        if ('"' != trailingQuote) {
            throw new JsonParseException("Unmatched quote at (" + startCol + ", " + startRow + ")");
        }
        return token;
    }

    private JsonToken parseJsonLiteral(Reader reader, int c) throws IOException {
        String parsed = StringUtils.parseToWhitespaceOrDelimiter(
                reader, JsonToken.JSON_TOKENS.keySet()).toLowerCase();
        // only count the parsed string, we already counted the prepended char
        String literal = "" + (char) c + incCursor(parsed);

        boolean isNumber = numberLiteralRegex.matcher(literal).matches();
        boolean isBoolean = boolLiteralRegex.matcher(literal).matches();
        boolean isNull = nullLiteralRegex.matcher(literal).matches();
        int start = col;

        if (isNumber) {
            return new JsonToken(JsonTokenType.LITERAL_NUMBER, literal);
        } else if (isBoolean) {
            return new JsonToken(JsonTokenType.LITERAL_BOOLEAN, literal);
        } else if (isNull) {
            return new JsonToken(JsonTokenType.LITERAL_NULL, literal);
        }
        throw new JsonInvalidLiteralException("Unexpected literal value at row (" + row + ") columns (" + start + " - "
                + col + "): '" + literal + "'");
    }

    private int readAndIncCursor() throws IOException {
        int c = reader.read();
        incCursor(c);
        return c;
    }

    private String incCursor(String string) {
        string.chars().forEach(this::incCursor);
        return string;
    }

    private void incCursor(int c) {
        if ('\n' == c) {
            row++;
            lastCol = col;
            col = 0;
        } else {
            col++;
        }
    }

    private void resetAndDecCursor(int c) throws IOException {
        if ('\n' == c) {
            row--;
            col = lastCol;
        } else {
            col--;
        }
        reader.reset();
    }
}

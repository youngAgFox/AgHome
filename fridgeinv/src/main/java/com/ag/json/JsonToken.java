package com.ag.json;

import java.util.HashMap;

public class JsonToken {

    public static final HashMap<Integer, JsonTokenType> JSON_TOKENS = new HashMap<>();

    static {
        for (JsonTokenType tokenType : JsonTokenType.values()) {
            if (tokenType.startChar >= 0) {
                JSON_TOKENS.put(tokenType.startChar, tokenType);
            }
        }
    }

    public final String value;
    public final JsonTokenType type;
    public JsonParserCursor cursor;

    public JsonToken(JsonTokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    public JsonToken(JsonTokenType type, int c) {
        this.type = type;
        this.value = String.valueOf((char) c);
    }

    @Override
    public String toString() {
        return "<" + type.toString() + (null == cursor ? ">" : "> at " + cursor.toString());
    }
}

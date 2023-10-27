package com.ag.json;

public enum JsonTokenType {
    LITERAL_BOOLEAN(-1), LITERAL_NUMBER(-1), LITERAL_NULL(-1), STRING('"'), OBJECT_START('{'), 
    OBJECT_END('}'), OBJECT_ASSIGNMENT(':'), SEPARATOR(','), ARRAY_START('['), ARRAY_END(']');

    public final int startChar;
    private final String name;

    private JsonTokenType(int startChar) {
        this.startChar = startChar;
        name = name().toLowerCase().replace("_", " ") 
            + (startChar >= 0 ? " < " + (char) startChar + " >" : "");
    }

    @Override
    public String toString() {
        return name;
    }
}

package com.ag.json;

public class JsonParserCursor {
    public final int col;
    public final int row;

    public JsonParserCursor(int col, int row) {
        this.col = col;
        this.row = row;
    }

    @Override
    public String toString() {
        return "(" + col + ", " + row + ")";
    }
}

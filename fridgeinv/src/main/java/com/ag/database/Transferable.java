package com.ag.database;

import java.util.Map;

public interface Transferable {
    public void initialize(Map<String, String> params);
    public Map<String, String> toArgs();
    public String getParamString();
}

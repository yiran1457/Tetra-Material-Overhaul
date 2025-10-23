package net.yiran.tmo.core;

import net.yiran.tmo.ContextData;

import java.util.Map;

public interface IMaterialData {
    void setContextData(Map<String, ContextData> contextData);
    Map<String, ContextData> getContextData();
}

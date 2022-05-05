package com.lotuslabs.rest.domain.variables;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VariableContext {
    private final Map<String,Object> variables;

    public VariableContext() {
        this.variables = new ConcurrentHashMap<>();
    }

    public void setVariable(String variableName, Object value) {
        this.variables.put(variableName, value);
    }

    public Map<String,Object> toMap() {
        return new LinkedHashMap<>(variables);
    }

    public void putAll(Map<String,Object> newVariables) {
        this.variables.putAll(newVariables);
    }

    public Collection<String> getVariableNames() {
        return this.variables.keySet();
    }

    public Object getOrDefault(String variableName, String defaultValue) {
        return this.variables.getOrDefault(variableName, defaultValue);
    }

    public VariableContext cloneVariableContext() {
        final Map<String, Object> ret = new LinkedHashMap<>(this.variables);
        VariableContext context =  new VariableContext();
        context.putAll(ret);
        return context;
    }

    @Override
    public String toString() {
        return "VariableContext{" +
                "variables=" + variables +
                '}';
    }
}

package com.lotuslabs.rest.domain.variables;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.lotuslabs.rest.domain.variables.Variable.VariableType.NIL;

@Slf4j
public class VariableContext {
    private static final Object NIL_VALUE = "{{nil}}";

    private final Map<String,Object> variables;

    public VariableContext() {
        this.variables = new ConcurrentHashMap<>();
    }

    public void setNilVariable(String variableName) {
        setVariable(variableName, NIL_VALUE);
    }

    public void setVariable(String variableName, Object value) {
        if (value != null && variableName != null) {
            this.variables.put(variableName, value);
        }
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

    public Object getOrDefault(String variableName, Object defaultValue) {
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

    public void remove(String variableName) {
        this.variables.remove(variableName);
    }
}

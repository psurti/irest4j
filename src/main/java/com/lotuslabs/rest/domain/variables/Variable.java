package com.lotuslabs.rest.domain.variables;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Variable {
    private static final Pattern templates = Pattern.compile("\\{\\{([A-Za-z0-9._]+)(:*)([A-Za-z0-9.]*)\\}\\}");

    public void remove(VariableContext variableContext) {
        variableContext.remove(this.name);
    }

    enum VariableType {
        NIL,
        NUMBER,
        STRING,
        LIST,
        MAP
    }

    String name;
    Object value;
    VariableType type;

    public Variable(String name, Object value) {
        this.name = name;
        this.value = value;
        if (value instanceof String){
            this.type = VariableType.STRING;
        } else if (value instanceof Number) {
            this.type = VariableType.NUMBER;
        } else if (value instanceof List<?>) {
            this.type = VariableType.LIST;
        } else if (value instanceof Map<?,?>) {
            this.type = VariableType.MAP;
        } else {
            this.type = VariableType.NIL;
        }
    }

    public String resolveJsonValue(VariableContext context) {
        return resolveValue(context, true);
    }

    public String resolveValue(VariableContext context) {
        return resolveValue(context, false);
    }

    private String resolveValue(VariableContext context, final boolean formatValue) {
        if (this.value == null) return null;
        String strValue = String.valueOf(this.value);
        final AtomicReference<String> substitutedData = new AtomicReference<>(strValue);
        final List<Object[]> variableCtx = new ArrayList<>();

        final Matcher matcher = templates.matcher(substitutedData.get());
        while (matcher.find()) {
            String variableName = matcher.group(1);
            final String separator = matcher.group(2);
            final String val = matcher.group(3);
            final String defaultValue = (!separator.equals(":")) ?
                    System.getenv(variableName) : val;
            if (variableName != null) {
                Object contextValue = context.getOrDefault(variableName, defaultValue);
                variableCtx.add(new Object[]{variableName, separator, val, contextValue});
            }
        }

        variableCtx.forEach(t -> {
            if (t[3] != null) {
                if (formatValue && t[3] instanceof String) {
                    t[3] = "\"" + t[3] + "\"";

                }
                String val = substitutedData.get().replaceAll("\\{\\{" + t[0] + t[1] + t[2] + "\\}\\}",  String.valueOf(t[3]));
                log.debug("=>{}", val);
                substitutedData.set(val);
            }
        });
        context.setVariable(this.name, substitutedData.get());
        this.value = substitutedData.get();
        return substitutedData.get();

    }

    public static Variable create(String key, List<String> values) {
        Variable variable = null;
        if (key != null) {
            variable = new Variable(key, values);
        }
        return variable;
    }

    public static Variable create(String key, String value) {
        Variable variable = null;
        if (key != null) {
            variable = new Variable(key, value);
        }
        return variable;
    }

    @Override
    public String toString() {
        return "Variable{" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", type=" + type +
                '}';
    }

    public String value() {
        return String.valueOf(value);
    }

    public String name() {
        return this.name;
    }
}

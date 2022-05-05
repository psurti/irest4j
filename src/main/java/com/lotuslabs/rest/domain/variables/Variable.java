package com.lotuslabs.rest.domain.variables;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Variable {
    private static final Pattern templates = Pattern.compile("\\{\\{([A-Za-z0-9._]+)(:*)([A-Za-z0-9.]*)\\}\\}");

    String name;
    Object value;
    int type;

    String resolveValue(VariableContext context) {
        if (this.value == null) return null;
        String strValue = String.valueOf(this.value);
        final AtomicReference<String> substitutedData = new AtomicReference<>(strValue);
        final List<String[]> variableCtx = new ArrayList<>();

        final Matcher matcher = templates.matcher(substitutedData.get());
        while (matcher.find()) {
            final String variableName = matcher.group(1);
            final String separator = matcher.group(2);
            final String val = matcher.group(3);
            final String defaultValue = (!separator.equals(":")) ?
                    System.getenv(variableName) : val;
            if (variableName != null) {
                String variableValue = null;
                Object contextValue = context.getOrDefault(variableName, defaultValue);
                if (contextValue != null) {
                    variableValue = String.valueOf(contextValue);
                }
                variableCtx.add(new String[]{variableName, separator, val, variableValue});
            }
        }
        variableCtx.forEach(t -> {
            if (t[3] != null) {
                String val = substitutedData.get().replaceAll("\\{\\{" + t[0] + t[1] + t[2] + "\\}\\}", t[3]);
                log.info("===>{}", val);
                substitutedData.set(val);
            }
        });
        context.setVariable(this.name, substitutedData.get());
        return substitutedData.get();

    }

    public static Variable create(String key, String value) {
        Variable variable = null;
        if (key != null) {
            variable = new Variable();
            variable.name = key;
            variable.value = value;
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
}

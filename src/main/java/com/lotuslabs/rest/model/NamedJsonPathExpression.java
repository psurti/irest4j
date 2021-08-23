package com.lotuslabs.rest.model;

public class NamedJsonPathExpression {
    public static final String REGEX = "regex:";
    private String jsonPath;
    private String jsonPathLabel;
    private String expectedValue;

    public NamedJsonPathExpression() {}

    public static NamedJsonPathExpression valueOf(String jsonPathLabel, String jsonPath) {
        return new NamedJsonPathExpression().setJsonPath(jsonPath).setJsonPathLabel(jsonPathLabel);
    }
    
    public NamedJsonPathExpression setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
        return this;
    }

    public NamedJsonPathExpression setJsonPathLabel(String jsonPathLabel) {
        this.jsonPathLabel = jsonPathLabel;
        return this;
    }

    public String getJsonPath() {
        return jsonPath;
    }

    public String getJsonPathLabel() {
        return jsonPathLabel;
    }

    public NamedJsonPathExpression setExpectedValue(String expectedValue) {
        this.expectedValue = expectedValue;
        return this;
    }

    public boolean checkValue(String actualValue) {
        if (expectedValue.startsWith(REGEX)) {
            return actualValue != null && actualValue.matches(this.expectedValue.substring(REGEX.length()));
        }
        return actualValue != null && actualValue.equals(this.expectedValue);
    }

    public String getExpectedValue() {
        return this.expectedValue;
    }
}

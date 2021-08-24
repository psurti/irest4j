package com.lotuslabs.rest.model;

public class NamedJsonPathExpression {
    public static final String REGEX = "regex:";
    private String jsonPath;
    private String jsonPathLabel;
    private String expectedValue;
    private boolean isRegexExpectedValue;
    private String resolvedExpectedValue;

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
        this.resolvedExpectedValue = expectedValue;
        if (isRegex(expectedValue)) {
            this.isRegexExpectedValue = true;
            this.expectedValue = extractExpectedValue(expectedValue);
            this.resolvedExpectedValue = this.expectedValue;
        }
        return this;
    }

    public boolean checkValue(String actualValue) {
        if (isRegexExpectedValue) {
            return actualValue != null && actualValue.matches(this.resolvedExpectedValue);
        }
        return actualValue != null && actualValue.equals(this.expectedValue);
    }

    public String getResolvedExpectedValue() {
        return this.resolvedExpectedValue;
    }

    public void setResolvedExpectedValue(String value) {
        this.resolvedExpectedValue = value;
    }

    public String getExpectedValue() {
        return this.expectedValue;
    }

    private boolean isRegex(String value) {
        return value != null && value.startsWith(REGEX);
    }

    private String extractExpectedValue(String value) {
        if (isRegex(value)) {
            return value.substring(REGEX.length());
        }
        return value;
    }
}

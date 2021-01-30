package com.lotuslabs.rest.model;

public class NamedJsonPathExpression {
    private String jsonPath;
    private String jsonPathLabel;

    private NamedJsonPathExpression() {}

    public static NamedJsonPathExpression valueOf(String jsonPathLabel, String jsonPath) {
        return new NamedJsonPathExpression().setJsonPath(jsonPath).setJsonPathLabel(jsonPathLabel);
    }
    
    private NamedJsonPathExpression setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
        return this;
    }

    private NamedJsonPathExpression setJsonPathLabel(String jsonPathLabel) {
        this.jsonPathLabel = jsonPathLabel;
        return this;
    }

    public String getJsonPath() {
        return jsonPath;
    }

    public String getJsonPathLabel() {
        return jsonPathLabel;
    }
}

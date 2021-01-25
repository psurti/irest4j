package com.lotuslabs.rest.model;

public class JsonPathParam {
    private String jsonPath;
    private String jsonPathLabel;

    private JsonPathParam() {}

    public static JsonPathParam valueOf(String jsonPathLabel, String jsonPath) {
        return new JsonPathParam().setJsonPath(jsonPath).setJsonPathLabel(jsonPathLabel);
    }
    
    private JsonPathParam setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
        return this;
    }

    private JsonPathParam setJsonPathLabel(String jsonPathLabel) {
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

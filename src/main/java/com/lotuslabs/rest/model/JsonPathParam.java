package com.lotuslabs.rest.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class JsonPathParam {
    private String jsonPath;
    private String jsonPathLabel;

    private JsonPathParam() {}

    public static JsonPathParam valueOf(String jsonPathLabel, String jsonPath) {
        return new JsonPathParam().setJsonPath(jsonPath).setJsonPathLabel(jsonPathLabel);
    }

    public static JsonPathParam[] valueOf(String name, Properties p) {
        List<JsonPathParam> ret = new ArrayList<>();
        p.forEach( (k, v) -> {
            final int index = k.toString().indexOf(name + ".jsonPath.");
            if (index >= 0) {
                final String jsonPathLabel = k.toString().substring((name + ".jsonPath.").length());
                ret.add(JsonPathParam.valueOf(jsonPathLabel, v.toString()));
            }
        });
        return ret.toArray(new JsonPathParam[0]);
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

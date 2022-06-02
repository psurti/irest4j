package com.lotuslabs.rest.adapters.config;

import com.lotuslabs.rest.domain.configuration.Configurable;
import com.lotuslabs.rest.domain.variables.Variable;
import com.lotuslabs.rest.domain.variables.VariableContext;
import com.lotuslabs.rest.domain.variables.VariableSet;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
public class Configuration implements Configurable {
    private final SimplePropertiesConfig propertiesConfig;

    public Configuration(SimplePropertiesConfig propertiesConfig) {
        this.propertiesConfig = propertiesConfig;
    }


    public Map<String,String> getRequestHeaders(String name) {
        return propertiesConfig.startsWithProperties("http." + name + ".headers.");
    }

    public Variable getRequestBody(String name) {
        final String variableName = "http." + name + ".body";
        final String value = propertiesConfig.getProperty(variableName, null);
        final String bodyData = getBodyData(value, propertiesConfig.getPropertyPath());
        return Variable.create("body", bodyData);
    }

    @Override
    public VariableContext createInitialContext() {
        VariableContext variableContext = new VariableContext();
        getGlobalVariables().resolveValues(variableContext);
        getAllRequestVariables().resolveValues(variableContext);
        return variableContext;
    }

    public VariableContext createRequestContext(final VariableContext initialContext, String name) {
        VariableContext context = initialContext.cloneVariableContext();
        getRequestVariables(name).resolveValues(context);
        return initialContext;
    }

    public VariableContext addResponseContext(final VariableContext requestContext, final String name) {
        getAssignVariables(name).resolveValues(requestContext);
        return requestContext;
    }

    @Override
    public VariableSet getGlobalVariables() {
        final Map<String, String> vars = propertiesConfig.startsWithProperties("vars.");
        return VariableSet.create(vars);
    }

    @Override
    public VariableSet getAllRequestVariables() {
        return VariableSet.create(propertiesConfig.startsWithProperties("http.vars."));
    }

    @Override
    public VariableSet getRequestVariables(String name) {
        return VariableSet.create(propertiesConfig.startsWithProperties("http." + name + ".vars."));
    }

    @Override
    public VariableSet getAssignVariables(String name) {
        return VariableSet.create(propertiesConfig.startsWithProperties("http." + name + ".response.assign["));
    }

    public VariableSet getEvaluateVariables(String name) {
        return VariableSet.create(propertiesConfig.startsWithProperties("http." + name + ".response.eval["));
    }

    public VariableSet getAssertVariables(String name) {
        return VariableSet.create(propertiesConfig.nextPartialMatchProperties("http." + name + ".response.assert["));
    }

    public Variable getAbsUrl(String name) {
        String url = propertiesConfig.getProperty("vars.url", null);
        url = propertiesConfig.getProperty("http.vars.url", url);
        return Variable.create("url",
                url + propertiesConfig.getProperty("http." + name + ".uri", ""));
    }

    public String[] getAllActionNames() {
        return propertiesConfig.nextPartialMatch(
                "http.get",
                "http.post",
                "http.put",
                "http.patch",
                "http.delete",
                "http.form"
        ).toArray(new String[0]);
    }

    private String getBodyData(String body, String propertiesPath) {
        if (body != null && body.endsWith(".json")) {
            try {
                body = new String(Files.readAllBytes(Paths.get(propertiesPath, body)));
            } catch (IOException e) {
                log.error(e.getLocalizedMessage(), e);
            }
        }
        return body;
    }

}

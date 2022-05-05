package com.lotuslabs.rest.domain.configuration;

import com.lotuslabs.rest.domain.variables.Variable;
import com.lotuslabs.rest.domain.variables.VariableContext;
import com.lotuslabs.rest.domain.variables.VariableSet;

import java.util.Map;

public interface Configurable {
    Map<String,String> getRequestHeaders(String name);

    Variable getRequestBody(String name);

    VariableContext createInitialContext();

    VariableSet getGlobalVariables();

    VariableSet getAllRequestVariables();

    VariableSet getRequestVariables(String name);

    VariableSet getResponseVariables(String name);

    VariableSet getVariables(String propertyPath);

    Variable getAbsUrl(String name);

    String[] getAllActionNames();
}

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

    VariableSet getAssignVariables(String name);

    VariableSet getEvaluateVariables(String name);

    VariableSet  getAssertVariables(String name);

    Variable getAbsUrl(String name);

    String[] getAllActionNames();


}

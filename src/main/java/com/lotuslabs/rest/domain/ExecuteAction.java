package com.lotuslabs.rest.domain;

import com.jayway.jsonpath.JsonPath;
import com.lotuslabs.rest.adapters.config.http.RequestEntityFactory;
import com.lotuslabs.rest.adapters.http.RestTemplateClient;
import com.lotuslabs.rest.adapters.spel.MapAccessor;
import com.lotuslabs.rest.domain.configuration.Configurable;
import com.lotuslabs.rest.domain.variables.Variable;
import com.lotuslabs.rest.domain.variables.VariableContext;
import com.lotuslabs.rest.domain.variables.VariableSet;
import net.minidev.json.JSONValue;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ExecuteAction {
    public void execute(Configurable configurable, RestTemplateClient client) throws IOException {
        VariableContext variableContext = configurable.createInitialContext();

        final String[] actionNames = configurable.getAllActionNames();
        RequestEntity<?> requestEntity = null;
        for (String actionName : actionNames) {
            if (actionName.startsWith("delete")) {
                requestEntity = RequestEntityFactory.createDeleteRequestEntity(variableContext, actionName, configurable).build();
            } else if (actionName.startsWith("get")) {
                requestEntity = RequestEntityFactory.createGet(variableContext, actionName, configurable).build();
            } else if (actionName.startsWith("post")) {
                requestEntity = RequestEntityFactory.createPostRequestEntity(variableContext, actionName, configurable);
            } else if (actionName.startsWith("patch")) {
                requestEntity = RequestEntityFactory.createPatchRequestEntity(variableContext, actionName, configurable);
            }  else if (actionName.startsWith("put")) {
                requestEntity = RequestEntityFactory.createPutRequestEntity(variableContext, actionName, configurable);
            }  else if (actionName.startsWith("form")) {
                requestEntity = RequestEntityFactory.createFormPostRequestEntity(variableContext, actionName, configurable);
            }
            final ResponseEntity<String> result = client.exchange(requestEntity, String.class);
            System.out.println(result.getStatusCode());

            final String resultBody = result.getBody();
            final String statusCode = result.getStatusCode().name();
            final int statusCodeValue = result.getStatusCodeValue();
            final VariableSet responseVariables = configurable.getResponseVariables(actionName);
            final VariableSet expectationVariables = configurable.getExpectationVariables(actionName);

            evaluate(variableContext,
                    resultBody, statusCode, statusCodeValue,
                    responseVariables, expectationVariables);

        }

    }

     void evaluate(VariableContext variableContext,
                   final String resultBody, final String statusCode, final int statusCodeValue,
                          VariableSet responseVariables, VariableSet expectationVariables) throws IOException {
        variableContext.setVariable("status", statusCode);
        variableContext.setVariable("statusCode", statusCodeValue);
        if (resultBody != null) {
            if ((resultBody.startsWith("{") && resultBody.endsWith("}"))
                    || ((resultBody.startsWith("[") && resultBody.endsWith("]")))) {
                variableContext.setVariable("json", resultBody);
                evaluateJson(variableContext, responseVariables);
                expect(variableContext, expectationVariables);
                // System.out.println(variableContext);
            }
        }
    }

    private void evaluateJson(VariableContext variableContext, VariableSet responseVariables) throws IOException {
        final Map<String, Object> entries = responseVariables.toMap();
        final String response = String.valueOf(variableContext.getOrDefault("json","{}"));
        for (String key : entries.keySet()) {
            Object val = entries.get(key);
            System.out.println( "evaluation exp:" + val);
            final Object jpResult = JsonPath.parse(response).read(val.toString());
            System.out.println( "evaluated obj:" + jpResult);
            if (jpResult instanceof List && ((List<?>) jpResult).size() == 1) {
                Object objVal = ((List<?>) jpResult).get(0);
                variableContext.setVariable(key, objVal);
                variableContext.setVariable(key + ".json", objVal);
                if (objVal instanceof String) {
                    variableContext.setVariable(key + ".json", "\"" + objVal + "\"");
                }
            } else if (jpResult instanceof Map && ((Map<?, ?>) jpResult).size() == 1) {
                final Object objVal = ((Map<?, ?>) jpResult).values().toArray()[0];
                variableContext.setVariable(key, objVal);
                variableContext.setVariable(key + ".json", objVal);
                if (objVal instanceof String) {
                    variableContext.setVariable(key + ".json", "\"" + objVal + "\"" );
                }
            } else {
                StringBuilder jsonData = new StringBuilder();
                JSONValue.writeJSONString(jpResult, jsonData);
                variableContext.setVariable(key, jpResult);
                variableContext.setVariable(key + ".json", jsonData.toString());
            }
        }
    }

    private void expect(VariableContext variableContext, VariableSet expectationVariables) throws IOException {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.addPropertyAccessor(new MapAccessor());
        final SpelExpressionParser parser = new SpelExpressionParser();
        context.setVariable("json", variableContext.toMap());
        final Collection<Variable> variables = expectationVariables.getVariables();
        for (Variable variable : variables) {
            System.out.println( variable );
            final String exp = variable.resolveJsonValue(variableContext);
            System.out.println( "expectation exp:" + exp);
            final Expression expression = parser.parseExpression(exp);
            final Object value = expression.getValue(context, Object.class);
            System.out.println( "#" + variable.name() + "=" + value);
            context.setVariable(variable.name(), value);
            StringBuilder jsonData = new StringBuilder();
            JSONValue.writeJSONString(value, jsonData);
            variableContext.setVariable(variable.name(), value);
            variableContext.setVariable(variable.name() + ".json", jsonData.toString());
        }
    }
}

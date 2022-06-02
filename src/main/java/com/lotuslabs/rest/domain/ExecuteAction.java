package com.lotuslabs.rest.domain;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.internal.JsonFormatter;
import com.lotuslabs.rest.adapters.http.RequestEntityFactory;
import com.lotuslabs.rest.adapters.http.RestTemplateClient;
import com.lotuslabs.rest.adapters.spel.MapAccessor;
import com.lotuslabs.rest.domain.configuration.Configurable;
import com.lotuslabs.rest.domain.variables.Variable;
import com.lotuslabs.rest.domain.variables.VariableContext;
import com.lotuslabs.rest.domain.variables.VariableSet;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONValue;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.lotuslabs.rest.domain.AnsiCode.*;

@Slf4j
public class ExecuteAction {

    public static final String DOT_JSON = ".json";

    public void execute(Configurable configurable, RestTemplateClient client) throws IOException {
        VariableContext variableContext = configurable.createInitialContext();

        final String[] actionNames = configurable.getAllActionNames();
        RequestEntity<?> requestEntity = null;
        for (String actionName : actionNames) {
            requestEntity = RequestEntityFactory.createRequestEntity(variableContext, actionName, configurable);
            final ResponseEntity<String> result = client.exchange(requestEntity, String.class);
            log.info("{}{}({}){}", ANSI_CYAN, actionName, result.getStatusCode(), ANSI_RESET);
            log.info("{}{}{}", ANSI_CYAN, requestEntity.toString().split(",")[0], ANSI_RESET);
            final String rBody = result.getBody();
            final String resultBody = rBody != null ? rBody.trim() : null;
            final String statusCode = result.getStatusCode().name();
            final int statusCodeValue = result.getStatusCodeValue();
            final VariableSet headerVariables = VariableSet.create(result.getHeaders());
            final VariableSet assignVariables = configurable.getAssignVariables(actionName);
            final VariableSet evaluateVariables = configurable.getEvaluateVariables(actionName);
            final VariableSet assertVariables  = configurable.getAssertVariables(actionName);
            evaluateVariables.addVariables(assertVariables);
            /*
            final Variable assertVariable = configurable.getAssertVariable(actionName);
            evaluateVariables.addVariable(assertVariable);
             */
            final Variable etagVariable = headerVariables.getVariable(HttpHeaders.ETAG);
            if (etagVariable != null) {
                variableContext.setVariable(HttpHeaders.ETAG, etagVariable );
            }
            evaluate(variableContext, headerVariables,
                    resultBody, statusCode, statusCodeValue,
                    assignVariables, evaluateVariables);

            assertion(variableContext, statusCodeValue);
        }

    }

    private void assertion(VariableContext variableContext, int statusCodeValue) {
        final Object assertionValue = variableContext.getOrDefault("assert",
                HttpStatus.Series.valueOf(statusCodeValue) == HttpStatus.Series.SUCCESSFUL);
        if (assertionValue == Boolean.TRUE) {
            log.info("{} ASSERT  {}{} OK {}", ANSI_PURPLE, ANSI_RESET,
                    ANSI_GREEN, ANSI_RESET);
        } else {
            log.info("{} ASSERT  {}{} FAILED {}", ANSI_PURPLE, ANSI_RESET,
                    ANSI_RED, ANSI_RESET);
        }
    }

    void evaluate(VariableContext variableContext, VariableSet headerVariables,
                   final String resultBody, final String statusCode, final int statusCodeValue,
                   VariableSet assignVariables, VariableSet evaluationVariables) throws IOException {
        variableContext.setVariable("status", statusCode);
        variableContext.setVariable("statusCode", statusCodeValue);
        if (resultBody != null && resultBody.startsWith("{") && resultBody.endsWith("}") ||
                resultBody != null && resultBody.startsWith("[") && resultBody.endsWith("]")
        ) {

                variableContext.setVariable("json", resultBody);
                // Arrange
                assignJson(variableContext, assignVariables);
                // Act
                evaluate(variableContext, evaluationVariables);
        }
    }

    private void assignJson(VariableContext variableContext, VariableSet responseVariables) throws IOException {
        final Map<String, Object> entries = responseVariables.toMap();
        final String response = String.valueOf(variableContext.getOrDefault("json","{}"));
        for (String key : entries.keySet()) {
            Object val = entries.get(key);
            log.debug( "evaluation exp: {}", val);
            final Object jpResult;
            try {
                jpResult = JsonPath.parse(response).read(val.toString());
            } catch (RuntimeException e ) {
                log.error(e.getLocalizedMessage());
                log.warn("eval:" + val + "-" + JsonFormatter.prettyPrint(response) );
                continue;
            }
            log.debug( "evaluated obj:" + jpResult);
            if (jpResult instanceof List && ((List<?>) jpResult).size() == 1) {
                Object objVal = ((List<?>) jpResult).get(0);
                variableContext.setVariable(key, objVal);
                variableContext.setVariable(key + DOT_JSON, objVal);
                if (objVal instanceof String) {
                    variableContext.setVariable(key + DOT_JSON, "\"" + objVal + "\"");
                }
            } else if (jpResult instanceof Map && ((Map<?, ?>) jpResult).size() == 1) {
                final Object objVal = ((Map<?, ?>) jpResult).values().toArray()[0];
                variableContext.setVariable(key, objVal);
                variableContext.setVariable(key + DOT_JSON, objVal);
                if (objVal instanceof String) {
                    variableContext.setVariable(key + DOT_JSON, "\"" + objVal + "\"" );
                }
            } else {
                StringBuilder jsonData = new StringBuilder();
                JSONValue.writeJSONString(jpResult, jsonData);
                if (jpResult instanceof Collection && ((Collection<?>) jpResult).isEmpty()) {
                    variableContext.setNilVariable(key);
                } else {
                    variableContext.setVariable(key, jpResult);
                }
                variableContext.setVariable(key + DOT_JSON, jsonData.toString());
            }
            log.info("{} ASSIGN   {}{}={} {}({}){}",
                    ANSI_PURPLE,
                    ANSI_RESET,
                    key + DOT_JSON,
                    variableContext.getOrDefault(key + DOT_JSON, null),
                    ANSI_BLUE,
                    val,
                    ANSI_RESET
            );
        }
    }

    private void evaluate(VariableContext variableContext, VariableSet expectationVariables) throws IOException {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.addPropertyAccessor(new MapAccessor());
        final SpelExpressionParser parser = new SpelExpressionParser();
        context.setVariable("json", variableContext.toMap());
        final Collection<Variable> variables = expectationVariables.getVariables();
        for (Variable variable : variables) {
            log.debug( variable.toString() );
            // remove the variable from context first
            variable.remove(variableContext);
            final String exp = variable.resolveJsonValue(variableContext);
            log.warn( "expectation exp: {}" , exp);
            final Expression expression = parser.parseExpression(exp);
            final Object value;
            try {
                value = expression.getValue(context, Object.class);
            } catch (RuntimeException e ) {
                log.error(e.getLocalizedMessage());
                continue;
            }
            context.setVariable(variable.name(), value);
            final StringBuilder jsonData = new StringBuilder();
            JSONValue.writeJSONString(value, jsonData);
            variableContext.setVariable(variable.name() + DOT_JSON, jsonData.toString());
            variableContext.setVariable(variable.name(), value);
            log.info("{} EVALUATE {}{}={} {}({}){}",
                    ANSI_PURPLE, ANSI_RESET,
                    variable.name(), value,
                    ANSI_BLUE,
                    exp,
                    ANSI_RESET);
        }
    }
}

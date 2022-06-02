package com.lotuslabs.rest.adapters.spel;

import com.lotuslabs.rest.domain.variables.Repository;
import com.lotuslabs.rest.domain.variables.VariableContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;

public class ContextRepository implements Repository<StandardEvaluationContext> {

    ContextRepository() {}

    public  StandardEvaluationContext checkOut(VariableContext context, Map<String,Object> data) {
        context.putAll(data);
        StandardEvaluationContext ret = new StandardEvaluationContext();
        ret.setVariables(context.toMap());
        return ret;
    }

    public  VariableContext checkIn(VariableContext context, StandardEvaluationContext evaluationContext) {
        for (String variableName : context.getVariableNames()) {
            final Object o = evaluationContext.lookupVariable(variableName);
            if (o != null) {
                context.setVariable(variableName, o);
            }
        }
        return context;
    }
}

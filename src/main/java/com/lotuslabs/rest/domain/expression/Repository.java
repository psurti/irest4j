package com.lotuslabs.rest.domain.expression;

import com.lotuslabs.rest.domain.variables.VariableContext;

public interface Repository {
    void evaluate(VariableContext context);
}

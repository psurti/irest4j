package com.lotuslabs.rest.domain.variables;

import java.util.Map;

public interface Repository<K> {
    K checkOut(VariableContext context, Map<String, Object> data);
    VariableContext checkIn(VariableContext context, K evaluationContext);
}

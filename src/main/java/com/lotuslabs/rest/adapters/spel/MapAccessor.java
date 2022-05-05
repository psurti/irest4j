package com.lotuslabs.rest.adapters.spel;

import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;

import java.util.Map;

public class MapAccessor implements PropertyAccessor {

    @Override
    public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
        return (((Map<?, ?>) target).containsKey(name));
    }

    @Override
    public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
        return new TypedValue(((Map<?, ?>) target).get(name));
    }

    @Override
    public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
        ((Map<Object, Object>) target).put(name, newValue);
    }

    @Override
    public Class<?>[] getSpecificTargetClasses() {
        return new Class<?>[]{Map.class};
    }
}

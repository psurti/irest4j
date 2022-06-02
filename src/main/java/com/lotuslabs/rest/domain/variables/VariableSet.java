package com.lotuslabs.rest.domain.variables;

import org.springframework.util.MultiValueMap;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VariableSet {
    private final LinkedHashMap<String, Variable> variables;


     public void resolveValues(VariableContext variableContext) {
        variables.forEach( (k, v) -> {
            v.resolveValue(variableContext);
        });
    }

    public static VariableSet create(MultiValueMap<String, String> mvp) {
        final VariableSet ret = new VariableSet();
        for (Map.Entry<String, List<String>> entry : mvp.entrySet()) {
            Variable variable = Variable.create(entry.getKey(), entry.getValue());
            if (variable != null) {
                ret.addVariable(variable);
            }
        }
        return ret;
    }

    public static VariableSet create(Map<String,String> objectMap) {
        VariableSet ret = new VariableSet();
        for (Map.Entry<String,String> entry : objectMap.entrySet()) {
            Variable variable = Variable.create(entry.getKey(), entry.getValue());
            if (variable != null) {
                ret.addVariable(variable);
            }
        }

        return ret;
    }

    public boolean isEmpty() {
        return this.variables == null || this.variables.isEmpty();
    }
    public void addVariable(Variable variable) {
        this.variables.put(variable.name, variable);
    }

    public void addVariables(VariableSet variableSet) {
        final Collection<Variable> variableCollection = variableSet.getVariables();
        for (Variable variable : variableCollection) {
            this.variables.put(variable.name, variable);
        }
    }

    public VariableSet(Variable... variables){
        this.variables = new LinkedHashMap<>();
        for (Variable v : variables) {
            this.variables.put(v.name, v);
        }
    }

    public Map<String,Object> toMap() {
        Map<String,Object> ret = new LinkedHashMap<>();
        this.variables.values().forEach( t -> ret.put(t.name, t.value));
        return ret;
    }

    public Variable getVariable(String name) {
        return this.variables.get(name);
    }

    public Collection<Variable> getVariables() {
         return this.variables.values();
    }

}


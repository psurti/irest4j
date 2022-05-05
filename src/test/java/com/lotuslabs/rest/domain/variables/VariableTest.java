 package com.lotuslabs.rest.domain.variables;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VariableTest {

    @Test
    void resolveValue() {
        String[] values = new String[] {
                "{{USERNAME}}",
                "{{USERNAME:yyy}}",
                "{{messsageId:3}}",
                "{{messageId}}"
        };

        String[] expect = new String[] {
                "psurti",
                "yyy",
                "3",
                "{{messageId}}"
        };


        for (int i = 0; i < values.length; i++) {
            Variable v = new Variable();
            v.name = "var" + i;
            v.value = values[i];
            String expectValue = expect[i];
            final String resolveValue = v.resolveValue(new VariableContext());
            assertEquals(expectValue, resolveValue);
        }
    }
}
package com.lotuslabs.rest.domain.variables;

import com.lotuslabs.rest.adapters.config.Configuration;
import com.lotuslabs.rest.adapters.config.LinkedProperties;
import com.lotuslabs.rest.adapters.config.SimplePropertiesConfig;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VariableSetTest {

    @Test
    void resolveValues() {
        String username = System.getProperty("user.name");
        LinkedProperties p = new LinkedProperties();
        p.setProperty("vars.url", "foo");
        p.setProperty("vars.user", "{{USERNAME}}");
        p.setProperty("vars.userId", "{{user}}");
        final SimplePropertiesConfig propertiesConfig = new SimplePropertiesConfig(Paths.get("."), p);
        Configuration configuration = new Configuration(propertiesConfig);
        final VariableSet globalVariables = configuration.getGlobalVariables();
        VariableContext context = new VariableContext();
        globalVariables.resolveValues(context);
        assertEquals("foo", context.getOrDefault("url", null));
        assertEquals(username, context.getOrDefault("user", null));
        assertEquals(username, context.getOrDefault("userId", null));
    }
}
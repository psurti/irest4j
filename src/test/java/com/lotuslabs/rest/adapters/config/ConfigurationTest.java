package com.lotuslabs.rest.adapters.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationTest {

    @Test
    void getRequestHeaders() {
        final String foo = String.format("x.%s.y", "foo");
        assertEquals("x.foo.y", foo);
    }
}
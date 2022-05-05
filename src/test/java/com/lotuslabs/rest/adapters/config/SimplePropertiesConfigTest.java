package com.lotuslabs.rest.adapters.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.*;

class SimplePropertiesConfigTest {

    @Test
    void startsWith() {

        Properties p = new Properties();
        p.setProperty("http.vars", "0");
        p.setProperty("http.getAllPosts", "1");
        p.setProperty("http.getAllPosts.description", "2");
        p.setProperty("http.getAllPosts.foo.getBar", "3");
        p.setProperty("http.getAllPosts1", "1");
        p.setProperty("http.getAllPosts1.description", "2");
        p.setProperty("http.getAllPosts1.foo.getBar", "3");
        final SimplePropertiesConfig propertiesConfig = new SimplePropertiesConfig(Paths.get("."), p);
        final Map<String, String> actual = propertiesConfig.startsWith("http.get");
        final Properties expected = new Properties();
        expected.putAll(p);
        expected.remove("http.vars");
        expected.remove("propertyPath");
        final TreeMap<String, String> sortedMap = new TreeMap<>();
        for (Map.Entry<Object, Object> entry : expected.entrySet()) {
            sortedMap.put(entry.getKey().toString(), entry.getValue().toString());
        }
        System.out.println( sortedMap );
        System.out.println( actual );
        Assertions.assertIterableEquals(sortedMap.keySet(), actual.keySet());

    }

    @Test
    void startsWithPrefixStrip_partialPrefix() {
        Properties p = new Properties();
        p.setProperty("http.vars", "0");
        p.setProperty("http.getAllPosts", "1");
        p.setProperty("http.getAllPosts.description", "2");
        p.setProperty("http.getAllPosts.foo.getBar", "3");
        p.setProperty("http.getAllPosts1", "4");
        p.setProperty("http.getAllPosts1.description", "5");
        p.setProperty("http.getAllPosts1.foo.getBar", "6");
        final SimplePropertiesConfig propertiesConfig = new SimplePropertiesConfig(Paths.get("."), p);
        final Map<String, String> actual = propertiesConfig.startsWithPrefixStrip("http.get");
        final TreeMap<String, String> expected = new TreeMap<>();
        expected.put("description", "2");
        expected.put("foo.getBar", "6");

        System.out.println( "expect:" + expected);
        System.out.println( "actual:" + actual );
        Assertions.assertIterableEquals(expected.keySet(), actual.keySet());
    }

    @Test
    void startsWithPrefixStrip_completePrefix() {
        Properties p = new Properties();
        p.setProperty("http.vars", "0");
        p.setProperty("http.getAllPosts", "1");
        p.setProperty("http.getAllPosts.description", "2");
        p.setProperty("http.getAllPosts.foo.getBar", "3");
        p.setProperty("http.getAllPosts1", "4");
        p.setProperty("http.getAllPosts1.description", "5");
        p.setProperty("http.getAllPosts1.foo.getBar", "6");
        final SimplePropertiesConfig propertiesConfig = new SimplePropertiesConfig(Paths.get("."), p);
        final Map<String, String> actual = propertiesConfig.startsWithPrefixStrip("http.");
        final TreeMap<String, String> expected = new TreeMap<>();
        expected.put("vars","0");
        expected.put("getAllPosts", "1");
        expected.put("getAllPosts.description", "2");
        expected.put("getAllPosts.foo.getBar", "3");
        expected.put("getAllPosts1", "4");
        expected.put("getAllPosts1.description", "5");
        expected.put("getAllPosts1.foo.getBar", "6");

        System.out.println( "expect:" + expected);
        System.out.println( "actual:" + actual );
        Assertions.assertIterableEquals(expected.keySet(), actual.keySet());
    }


    @Test
    void startsWithSuffixStrip_partialPrefix() {
        Properties p = new Properties();
        p.setProperty("http.vars", "0");
        p.setProperty("http.getAllPosts", "1");
        p.setProperty("http.getAllPosts.description", "2");
        p.setProperty("http.getAllPosts.foo.getBar", "3");
        p.setProperty("http.getAllPosts1", "4");
        p.setProperty("http.getAllPosts1.description", "5");
        p.setProperty("http.getAllPosts1.foo.getBar", "6");
        final SimplePropertiesConfig propertiesConfig = new SimplePropertiesConfig(Paths.get("."), p);
        final Map<String, String> actual = propertiesConfig.startsWithSuffixStrip("http.get");
        final TreeMap<String, String> expected = new TreeMap<>();
        expected.put("http.getAllPosts", "3");
        expected.put("http.getAllPosts1", "6");

        System.out.println( "expect:" + expected);
        System.out.println( "actual:" + actual );
        Assertions.assertIterableEquals(expected.keySet(), actual.keySet());
    }

    @Test
    void startsWithSuffixStrip_completePrefix() {
        Properties p = new Properties();
        p.setProperty("http.vars", "0");
        p.setProperty("http.getAllPosts", "1");
        p.setProperty("http.getAllPosts.description", "2");
        p.setProperty("http.getAllPosts.foo.getBar", "3");
        p.setProperty("http.getAllPosts1", "4");
        p.setProperty("http.getAllPosts1.description", "5");
        p.setProperty("http.getAllPosts1.foo.getBar", "6");
        final SimplePropertiesConfig propertiesConfig = new SimplePropertiesConfig(Paths.get("."), p);
        final Map<String, String> actual = propertiesConfig.startsWithSuffixStrip("http");
        final TreeMap<String, String> expected = new TreeMap<>();
        expected.put("http", "6");

        System.out.println( "expect:" + expected);
        System.out.println( "actual:" + actual );
        Assertions.assertIterableEquals(expected.keySet(), actual.keySet());
    }

    @Test
    void nextMatch_completePrefix() {
        Properties p = new Properties();
        p.setProperty("http.vars", "0");
        p.setProperty("http.getAllPosts", "1");
        p.setProperty("http.getAllPosts.description", "2");
        p.setProperty("http.getAllPosts.foo.getBar", "3");
        p.setProperty("http.getAllPosts1", "1");
        p.setProperty("http.getAllPosts1.description", "2");
        p.setProperty("http.getAllPosts1.foo.getBar", "3");
        final SimplePropertiesConfig propertiesConfig = new SimplePropertiesConfig(Paths.get("."), p);
        final Collection<String> actual = propertiesConfig.nextExactMatch("http");
        Collections.sort(new ArrayList<>(actual));
        final TreeMap<String, String> expected = new TreeMap<>();
        expected.put("vars", "0");
        expected.put("getAllPosts", "3");
        expected.put("getAllPosts1", "3");
        System.out.println( "expect:" + expected );
        System.out.println( "actual:" + actual );
        Assertions.assertIterableEquals(expected.keySet(), actual);
    }

    @Test
    void nextMatch_partialPrefix() {
        Properties p = new Properties();
        p.setProperty("http.vars", "0");
        p.setProperty("http.getAllPosts", "1");
        p.setProperty("http.getAllPosts.description", "2");
        p.setProperty("http.getAllPosts.foo.getBar", "3");
        p.setProperty("http.getAllPosts1", "1");
        p.setProperty("http.getAllPosts1.description", "2");
        p.setProperty("http.getAllPosts1.foo.getBar", "3");
        final SimplePropertiesConfig propertiesConfig = new SimplePropertiesConfig(Paths.get("."), p);
        final Collection<String> actual = propertiesConfig.nextPartialMatch("http.get");
        Collections.sort(new ArrayList<>(actual));
        final TreeMap<String, String> expected = new TreeMap<>();
        expected.put("getAllPosts", "3");
        expected.put("getAllPosts1", "3");
        System.out.println( "expect:" + expected );
        System.out.println( "actual:" + actual );
        Assertions.assertIterableEquals(expected.keySet(), actual);
    }
}

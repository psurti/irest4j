package com.lotuslabs.rest.adapters.config;

import lombok.extern.slf4j.Slf4j;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

@Slf4j
public class SimplePropertiesConfig {
    final LinkedProperties properties;
    private final String propertyPath;

    static SimplePropertiesConfig create(String propertyFile) throws IOException {
        LinkedProperties p = new LinkedProperties();
        try (FileReader fr = new FileReader(propertyFile)) {
            p.load(fr);
        }
        final Path parent = Paths.get(propertyFile).getParent();
        return new SimplePropertiesConfig(parent, p);
    }

    public SimplePropertiesConfig(Path parent, LinkedProperties properties) {
        if (properties == null)
            properties = new LinkedProperties();
        String propertyPath = ".";
        if (parent != null) {
            propertyPath = parent.toString();
        }
        properties.setProperty("propertyPath", propertyPath + "/");

        log.info("settings:{}", properties);
        this.properties = properties;
        this.propertyPath = propertyPath;
    }

    Map<String,String> startsWithProperties(String... propertyPrefixes) {
        return startsWithPrefixStrip(propertyPrefixes);
    }

    final Function<PropertyNameSeparator,String> removePrefix = s -> s.propertyName.substring(s.separatorOffset);
    final Function<PropertyNameSeparator,String> removeSuffix = s -> s.propertyName.substring(0, s.separatorOffset - 1);
    final Function<PropertyNameSeparator,String> matchedPrefix = s -> s.propertyName;

    final Function<PropertyNameSeparator,String> nextExactMatch = s -> {
        final int i = s.propertyName.indexOf('.', s.separatorOffset);
        if (i >= 0) {
            return s.propertyName.substring(s.separatorOffset, i);
        }
        return s.propertyName.substring(s.separatorOffset);
    };

    final Function<PropertyNameSeparator,String> nextMatch = s -> s.propertyName;


    final Function<PropertyNameSeparator,String> nextPartialMatch = s -> {
        String beginStr = s.propertyName.substring(0, s.separatorOffset - 1);
        final int i = beginStr.lastIndexOf('.', s.separatorOffset);
        if (i >= 0) {
            return beginStr.substring(i + 1);
        }
        return beginStr;
    };

    public String getPropertyPath() {
        return propertyPath;
    }

    private static class PropertyNameSeparator {
        String propertyName;
        int separatorOffset;
    }

    public  Map<String,String> startsWith(String... propertyPrefixes) {
        return getProperties(matchedPrefix, propertyPrefixes);
    }

    public  Map<String,String> startsWithPrefixStrip(String... propertyPrefixes) {
        return getProperties(removePrefix, propertyPrefixes);
    }

    public  Map<String,String> startsWithSuffixStrip(String... propertyPrefixes) {
        return getProperties(removeSuffix, propertyPrefixes);
    }

    public String getPropertyKey(String propertyKey, String defaultValue) {
        final Collection<String> ret = nextExactMatch(propertyKey);
        if (ret.isEmpty()) {
            return defaultValue;
        }
        return ret.toArray(new String[0])[0];
    }
    public Collection<String> nextExactMatch(String... propertyPrefixes) {
        final Map<String, String> ret = getProperties(nextExactMatch, propertyPrefixes);
        return ret.keySet();
    }

    public Collection<String> nextPartialMatch(String... propertyPrefixes) {
        final Map<String, String> ret = getProperties(nextPartialMatch, propertyPrefixes);
        return ret.keySet();
    }

    public Map<String,String> nextPartialMatchProperties(String... propertyPrefixes) {
        return getProperties(nextPartialMatch, propertyPrefixes);
    }

    private Map<String,String> getProperties(Function<PropertyNameSeparator,String> fx, String... propertyPrefixes) {
        final Map<String,String> ret = new LinkedHashMap<>();
        properties.forEach((k, v) -> {
            for (String propertyPrefix : propertyPrefixes) {
                boolean isTerminalPrefix = (propertyPrefix.endsWith("."));
                final int matchedPrefixIndex = k.indexOf(propertyPrefix);

                if (matchedPrefixIndex >= 0) { // http.getYYY
                    int completePrefixIndex = matchedPrefixIndex + propertyPrefix.length();
                    if (!isTerminalPrefix) {
                        final int i = k.indexOf('.', propertyPrefix.length());
                        if (i >= 0) {
                            completePrefixIndex =  i + 1;
                        } else {
                            completePrefixIndex = k.length();
                        }
                    }

                    PropertyNameSeparator pns = new PropertyNameSeparator();
                    pns.propertyName = k;
                    pns.separatorOffset = completePrefixIndex;
                    final String newPropertyName = fx.apply(pns);
                    if (!newPropertyName.isEmpty()) {
                        ret.put(newPropertyName, v);
                    }
                }
            }
        });
        return ret;
    }

    String getProperty(String name, String defaultValue) {
       return properties.getProperty(name, defaultValue);
    }

}

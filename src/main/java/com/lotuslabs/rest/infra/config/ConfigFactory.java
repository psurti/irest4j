package com.lotuslabs.rest.infra.config;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.InvalidPropertiesFormatException;

public class ConfigFactory {
    private ConfigFactory(){}

    public static PropertiesConfig create(String configFile) throws IOException {
        PropertiesConfig ret;
        if (configFile.endsWith(".yaml")) {
            ret = createYamlProperties(configFile);
        } else if (configFile.endsWith(".properties")) {
            ret =  PropertiesConfig.create(configFile);
        } else {
            throw new InvalidPropertiesFormatException("Unsupported file");
        }
        return ret;
    }

    private static PropertiesConfig createYamlProperties(String configFile) {
        YamlPropertiesFactoryBean bean = new YamlPropertiesFactoryBean();
        bean.setResources(new FileSystemResource(configFile));
        final Path parent = Paths.get(configFile).getParent();
        return new PropertiesConfig(parent, bean.getObject());
    }
}

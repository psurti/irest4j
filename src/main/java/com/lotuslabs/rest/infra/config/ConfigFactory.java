package com.lotuslabs.rest.infra.config;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.InvalidPropertiesFormatException;
import java.util.Objects;

public class ConfigFactory {
    private ConfigFactory() {
    }


    public static Collection<PropertiesConfig> createAll(String configDir) throws IOException {
        Collection<PropertiesConfig> ret = new ArrayList<>();
        PropertiesConfig fileConfig = create(configDir, false);
        if (fileConfig != null) {
            ret.add(fileConfig);
        } else {
            File dir = new File(configDir);
            File[] files = dir.listFiles();
            for (int i = 0; i < Objects.requireNonNull(files).length; i++) {
                PropertiesConfig pc = create(files[i].getAbsolutePath(), false);
                if (pc != null) {
                    ret.add(pc);
                }

            }
        }
        return ret;
    }

    public static PropertiesConfig create(String configFile, boolean throwException) throws IOException {
        PropertiesConfig ret = null;
        if (configFile.endsWith(".yaml")) {
            ret = createYamlProperties(configFile);
        } else if (configFile.endsWith(".properties")) {
            ret = PropertiesConfig.create(configFile);
        } else {
            if (throwException) {
                throw new InvalidPropertiesFormatException("Unsupported file");
            }
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

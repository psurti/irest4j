package com.lotuslabs.rest.adapters.config;

import com.lotuslabs.rest.domain.configuration.Configurable;
import com.lotuslabs.rest.domain.configuration.Factory;
import org.springframework.beans.factory.config.YamlMapFactoryBean;
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

public class ConfigFactory implements Factory {

    public ConfigFactory() { }

    public Collection<Configurable> createAll(String configDir) throws IOException {
        final Collection<Configurable> ret = new ArrayList<>();
        final Configurable fileConfig = create(configDir, false);
        if (fileConfig != null) {
            ret.add(fileConfig);
        } else {
            File dir = new File(configDir);
            File[] files = dir.listFiles();
            for (int i = 0; i < Objects.requireNonNull(files).length; i++) {
                Configurable pc = create(files[i].getAbsolutePath(), false);
                if (pc != null) {
                    ret.add(pc);
                }

            }
        }
        return ret;
    }

    public  Configurable create(String configFile, boolean throwException) throws IOException {
        SimplePropertiesConfig ret = null;
        if (configFile.endsWith(".yaml")) {
            ret = createYamlLinkedMap(configFile);
        } else if (configFile.endsWith(".properties")) {
            ret = SimplePropertiesConfig.create(configFile);
        } else {
            if (throwException) {
                throw new InvalidPropertiesFormatException("Unsupported file");
            }
        }
        return new Configuration(ret);
    }

    private static SimplePropertiesConfig createYamlProperties(String configFile) {
        YamlPropertiesFactoryBean bean = new YamlPropertiesFactoryBean();
        bean.setResources(new FileSystemResource(configFile));
        final Path parent = Paths.get(configFile).getParent();
        return new SimplePropertiesConfig(parent, new LinkedProperties(bean.getObject()));
    }

    public static SimplePropertiesConfig createYamlLinkedMap(String configFile) {
        YamlMapFactoryBean bean = new YamlMapFactoryBean();
        bean.setResources(new FileSystemResource(configFile));
        final Path parent = Paths.get(configFile).getParent();
        return new SimplePropertiesConfig(parent, new LinkedProperties(bean.getObject()));
    }
}

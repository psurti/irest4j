package com.lotuslabs.rest.domain.configuration;

import java.io.IOException;
import java.util.Collection;

public interface Factory {
    Configurable create(String configFile, boolean throwException) throws IOException;
    Collection<Configurable> createAll(String configDir) throws IOException;
}

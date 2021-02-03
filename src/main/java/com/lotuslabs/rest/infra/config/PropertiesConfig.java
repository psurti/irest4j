package com.lotuslabs.rest.infra.config;

import com.lotuslabs.rest.interfaces.IConfig;
import com.lotuslabs.rest.model.NamedJsonPathExpression;
import com.lotuslabs.rest.model.actions.*;
import lombok.extern.slf4j.Slf4j;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class PropertiesConfig implements IConfig {
    private final Properties properties;

    static PropertiesConfig create(String propertyFile) throws IOException {
        Properties p = new Properties();
        try(FileReader fr = new FileReader(propertyFile)) {
            p.load(fr);
        }
        final Path parent = Paths.get(propertyFile).getParent();
        return new PropertiesConfig(parent, p);
    }

    PropertiesConfig(Path parent, Properties properties) {
        if (properties == null)
            properties = new Properties();
        String propertyPath = ".";
        if (parent != null) {
            propertyPath = parent.toString();
        }
        properties.setProperty("propertyPath", propertyPath + "/");

        log.info("settings:{}", properties);
        this.properties = properties;
    }

    @Override
    public String getHost() {
        return properties.getProperty("host");
    }

    @Override
    public String getBearer() {
        return properties.getProperty("bearer", null);
    }

    @Override
    public String getBasicAuth() {
        return properties.getProperty("basicAuth", null);
    }

    @Override
    public boolean isPretty() {
        return Boolean.parseBoolean(properties.getProperty("pretty", "false"));
    }

    private String[] getActionValues() {
        String[] ret;
        String actions = properties.getProperty("actions");
        if (actions == null) {
            List<String> values = new ArrayList<>();
            //try to check if actions are in format actions[0], actions[1]...
            int i = 0;
            do {
                actions = properties.getProperty("actions[" + (i++) + "]");
                if (actions != null) {
                    values.add(actions);
                }
            } while (actions != null);
            ret = values.toArray(new String[0]);
        } else {
            ret = properties.getProperty("actions").split(",");
        }
        return ret;
    }

    @Override
    public RestAction[] getActions() {
        List<RestAction> actions = new ArrayList<>();
        //-- Actions
        final String[] propertyNames = getActionValues();
        final StringBuilder builder = new StringBuilder();
        for (String action : propertyNames) {
            RestAction restAction = null;
            //-- Special handling of Login Action
            if (LoginAction.NAME.equalsIgnoreCase(action)) {
                restAction = (new LoginAction(this));
            }

            if (restAction == null) {
                builder.setLength(0);
                for (char c : action.toCharArray()) {
                    if (c == '_' || Character.isUpperCase(c)) {
                        break;
                    } else {
                        builder.append(c);
                    }
                }
                String method = builder.toString();
                if (method.equalsIgnoreCase(GetAction.NAME)) {
                    restAction = new GetAction(action, this);
                } else if (method.equalsIgnoreCase(PostAction.NAME)) {
                    restAction = new PostAction(action, this);
                } else if (method.equalsIgnoreCase(PutAction.NAME)) {
                    restAction = new PutAction(action, this);
                } else if (method.equalsIgnoreCase(DeleteAction.NAME)) {
                    restAction = new DeleteAction(action, this);
                } else if (method.equalsIgnoreCase(FormPostAction.NAME)) {
                    restAction = new FormPostAction(action, this);
                }
            }
            if (restAction != null) {
                actions.add(restAction);
            }
        }
        return actions.toArray(new RestAction[0]);
    }

    @Override
    public String getPath(String name) {
        final String pathProperty = properties.getProperty(name + ".path");
        Objects.requireNonNull(pathProperty, name + ".path property is required");
        return pathProperty.trim();
    }

    @Override
    public boolean isEncodeUrl(String name) {
        return Boolean.parseBoolean(properties.getProperty(name + ".encodeUrl", "true"));
    }

    @Override
    public boolean isEncodePath(String name) {
        return Boolean.parseBoolean(properties.getProperty(name + ".encodePath", "false"));
    }

    @Override
    public String getBody(String name) {
        return getBodyData(properties.getProperty(name + ".body"),
                properties.getProperty("propertyPath", "."));    }

    private String getBodyData(String body, String propertiesPath) {
        if (body != null && body.endsWith(".json")) {
            try {
                body = new String(Files.readAllBytes(Paths.get(propertiesPath, body)));
            } catch (IOException e) {
                log.error(e.getLocalizedMessage(), e);
            }
        }
        return body;
    }

    @Override
    public Map<String, NamedJsonPathExpression> getJsonExps(String name) {
        Map<String, NamedJsonPathExpression> ret = new LinkedHashMap<>();
        properties.forEach( (k, v) -> {
            final String key = k.toString();
            final int index = key.indexOf(name + ".jsonPath.");
            if (index >= 0) {
                String id = (key.endsWith(".expect")) ? key.substring(0, key.length()-7) : key;
                NamedJsonPathExpression expression = ret.get(id);
                if (expression == null) {
                    expression = new NamedJsonPathExpression();
                }

                if (key.endsWith(".expect")) {
                    expression.setExpectedValue(v.toString());
                } else {
                    final String jsonPathLabel = key.substring((name + ".jsonPath.").length());
                    expression.setJsonPath(v.toString()).setJsonPathLabel(jsonPathLabel);
                }
                ret.put(id, expression);
            }
        });
        return ret;
    }

    @Override
    public String getConsulToken() {
        return properties.getProperty("consulToken", System.getenv("CONSUL_TOKEN"));
    }

    public Map<String,String> getContext() {
        return properties.entrySet().stream()
                .filter(e -> e.getKey().toString().startsWith("ctx."))
                .collect(Collectors.toMap(k -> k.getKey().toString(),
                        v -> v.getValue().toString()));

    }
}

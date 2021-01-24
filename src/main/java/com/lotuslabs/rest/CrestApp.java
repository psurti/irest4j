package com.lotuslabs.rest;

import com.lotuslabs.rest.infra.client.RestTemplateClient;
import com.lotuslabs.rest.model.actions.RestActions;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientResponseException;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
public class CrestApp {
    private final RestTemplateClient client;
    private final RestActions actions;
    static {
        LoggerContext loggerFactory = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerFactory.getLogger("com.jayway.jsonpath").setLevel(Level.INFO);
    }

    public CrestApp(Properties p ) {
        //-- Initial Context Map
        final Map<String, String> initialContext = p.entrySet().stream()
                .filter(e -> e.getKey().toString().startsWith("ctx."))
                .collect(Collectors.toMap(k -> k.getKey().toString(),
                        v -> v.getValue().toString()));

        //-- Client
        client = new RestTemplateClient(
                p.getProperty("bearer", null),
                p.getProperty("basicAuth", null),
                System.getenv("CONSUL_TOKEN"),
                p.getProperty("pretty", "false"),
                initialContext  );
        actions = new RestActions(p);
    }

    void executeAll() throws IOException {
        client.execute(actions.getActions());
    }

    static void run(String[] args) throws IOException {
        if (args == null || args.length == 0) {
            log.warn("Usage: RestApp <property file>");
            System.exit(0);
        }
        String propertyFile = args[0];
        Properties p = new Properties();
        try(FileReader fr = new FileReader(propertyFile)) {
            p.load(fr);
            String propertyPath = ".";
            final Path parent = Paths.get(propertyFile).getParent();
            if (parent != null) {
                propertyPath = parent.toString();
            }
            p.setProperty("propertyPath", propertyPath + "/");
        }
        log.info("settings:{}", p);
        CrestApp app = new CrestApp(p);
        app.executeAll();
    }

    public static void main(String[] args) {
        try {
            CrestApp.run(args);
        } catch (RestClientResponseException e ) {
            log.error(e.getResponseBodyAsString(), e);
        } catch (IOException e) {
            log.error(e.getLocalizedMessage() , e );
        }
    }
}

package com.lotuslabs.rest;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.lotuslabs.rest.adapters.config.ConfigFactory;
import com.lotuslabs.rest.adapters.http.RestTemplateClient;
import com.lotuslabs.rest.domain.ExecuteAction;
import com.lotuslabs.rest.domain.configuration.Configurable;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientResponseException;

import java.util.Collection;

@Slf4j
public class Application {
    static {
        LoggerContext loggerFactory = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerFactory.getLogger("com.jayway.jsonpath").setLevel(Level.INFO);
    }

    private final RestTemplateClient client;
    private final Configurable configurable;

    private final ExecuteAction action;

    public Application(Configurable configurable) {
        client = new RestTemplateClient(configurable);
        action = new ExecuteAction();
        this.configurable = configurable;
        System.out.println( configurable.getRequestHeaders("getAllPosts"));
        System.out.println( configurable.getAllRequestVariables());
        System.out.println( configurable.getRequestVariables("getAllPosts"));
    }

    void executeAll() {
        action.execute(configurable, client);
    }

    static void run(String[] args) throws Exception {
        if (args == null || args.length == 0) {
            log.warn("Usage: RestApp <property file>");
            System.exit(0);
        }
        String propertyFile = args[0];
        Collection<Configurable> configs = new ConfigFactory().createAll(propertyFile);

        for (Configurable config : configs) {
            Application app = new Application(config);
            app.executeAll();
        }
    }

    public static void main(String[] args) {
        try {
            Application.run(args);
        } catch (RestClientResponseException e) {
            log.error(e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }
}

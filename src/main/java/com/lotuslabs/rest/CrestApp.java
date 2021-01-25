package com.lotuslabs.rest;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.lotuslabs.rest.infra.client.RestTemplateClient;
import com.lotuslabs.rest.infra.config.PropertiesConfig;
import com.lotuslabs.rest.interfaces.IConfig;
import com.lotuslabs.rest.model.actions.RestAction;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;

@Slf4j
public class CrestApp {
    private final RestTemplateClient client;
    private final RestAction[] actions;
    static {
        LoggerContext loggerFactory = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerFactory.getLogger("com.jayway.jsonpath").setLevel(Level.INFO);
    }

    public CrestApp(IConfig config ) {
        //-- Client
        client = new RestTemplateClient(config);
        actions = config.getActions();
    }

    void executeAll() throws IOException {
        client.execute(actions);
    }

    static void run(String[] args) throws IOException {
        if (args == null || args.length == 0) {
            log.warn("Usage: RestApp <property file>");
            System.exit(0);
        }
        String propertyFile = args[0];
        PropertiesConfig config = PropertiesConfig.create(propertyFile);
        CrestApp app = new CrestApp(config);
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

package com.lotuslabs.rest;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.lotuslabs.rest.infra.client.RestTemplateClient;
import com.lotuslabs.rest.infra.config.ConfigFactory;
import com.lotuslabs.rest.infra.config.PropertiesConfig;
import com.lotuslabs.rest.interfaces.IConfig;
import com.lotuslabs.rest.interfaces.Result;
import com.lotuslabs.rest.model.actions.RestAction;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientResponseException;

import java.util.Collection;

@Slf4j
public class IRestApp {
    private final RestTemplateClient client;
    private final RestAction[] actions;
    private final Result result;

    static {
        LoggerContext loggerFactory = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerFactory.getLogger("com.jayway.jsonpath").setLevel(Level.INFO);
    }

    public IRestApp(IConfig config) {
        //-- Client
        result = new Result();
        client = new RestTemplateClient(config, result);
        actions = config.getActions();
    }

    void executeAll() throws Exception {
        client.execute(result, actions);
    }

    static void run(String[] args) throws Exception {
        if (args == null || args.length == 0) {
            log.warn("Usage: RestApp <property file>");
            System.exit(0);
        }
        String propertyFile = args[0];
        Collection<PropertiesConfig> configs = ConfigFactory.createAll(propertyFile);

        for (PropertiesConfig config : configs) {
            IRestApp app = new IRestApp(config);
            app.executeAll();
        }
    }

    public static void main(String[] args) {
        try {
            IRestApp.run(args);
        } catch (RestClientResponseException e) {
            log.error(e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }
}

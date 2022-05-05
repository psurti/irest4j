package com.lotuslabs.rest.domain;

import com.lotuslabs.rest.adapters.config.http.RequestEntityFactory;
import com.lotuslabs.rest.adapters.http.RestTemplateClient;
import com.lotuslabs.rest.domain.configuration.Configurable;
import com.lotuslabs.rest.domain.variables.VariableContext;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

public class ExecuteAction {




    public void execute(Configurable configurable, RestTemplateClient client) {
        VariableContext variableContext = configurable.createInitialContext();

        final String[] actionNames = configurable.getAllActionNames();
        RequestEntity<?> requestEntity = null;
        for (String actionName : actionNames) {
            if (actionName.startsWith("delete")) {
                requestEntity = RequestEntityFactory.createDeleteRequestEntity(variableContext, actionName, configurable).build();
            } else if (actionName.startsWith("get")) {
                requestEntity = RequestEntityFactory.createGet(variableContext, actionName, configurable).build();
            } else if (actionName.startsWith("post")) {
                requestEntity = RequestEntityFactory.createPostRequestEntity(variableContext, actionName, configurable);
            } else if (actionName.startsWith("patch")) {
                requestEntity = RequestEntityFactory.createPatchRequestEntity(variableContext, actionName, configurable);
            }  else if (actionName.startsWith("put")) {
                requestEntity = RequestEntityFactory.createPutRequestEntity(variableContext, actionName, configurable);
            }  else if (actionName.startsWith("form")) {
                requestEntity = RequestEntityFactory.createFormPostRequestEntity(variableContext, actionName, configurable);
            }
            System.out.println( variableContext);
            System.out.println( requestEntity );
            final ResponseEntity<String> result = client.exchange(requestEntity, String.class);
            System.out.println(result.getStatusCode());
            System.out.println(result.getBody());
        }

    }
}

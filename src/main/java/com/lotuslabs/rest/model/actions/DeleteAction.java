package com.lotuslabs.rest.model.actions;

import com.lotuslabs.rest.interfaces.IRestClient;
import com.lotuslabs.rest.model.RestContext;

import java.util.Map;

public class DeleteAction extends RestAction {
    public static final String NAME = "delete";

    public DeleteAction(String name) {
        super(name);
    }

    @Override
    public Map<String, ?> execute(RestContext restContext, IRestClient<Map<String, ?>, String> restClient) {
        String name = getName();
        return restClient.delete(restContext.getURI(name),
                restContext.getNamedJsonPathExpression(name));
    }
}
package com.lotuslabs.rest.model.actions;

import com.lotuslabs.rest.interfaces.IRestClient;
import com.lotuslabs.rest.model.RestContext;

import java.util.Map;

public class GetAction extends RestAction {
    public static final String NAME = "get";

    public GetAction(String name) {
        super(name);
    }

    @Override
    public Map<String, ?> execute(RestContext restContext,
                                  IRestClient<Map<String, ?>, String> restClient) {
        String name = getName();
        return restClient.get(restContext.getURI(name),
                restContext.getHeaders(name),
                restContext.getNamedJsonPathExpression(name));
    }
}

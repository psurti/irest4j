package com.lotuslabs.rest.model.actions;

import com.lotuslabs.rest.interfaces.IRestClient;
import com.lotuslabs.rest.model.RestContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class PostAction extends RestAction {
    public static final String NAME = "post";

    public PostAction(String name) {
        super(name);
    }

    @Override
    public Map<String, ?> execute(RestContext restContext, IRestClient<Map<String, ?>, String> restClient) {
        String name = getName();
        return restClient.post(restContext.getURI(name), restContext.getBodyString(name),
                restContext.getNamedJsonPathExpression(name));
    }
}


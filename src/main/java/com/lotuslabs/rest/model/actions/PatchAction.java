package com.lotuslabs.rest.model.actions;

import com.lotuslabs.rest.interfaces.IRestClient;
import com.lotuslabs.rest.model.RestContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class PatchAction extends RestAction {
    public static final String NAME = "patch";

    public PatchAction(String name) {
        super(name);
    }

    @Override
    public Map<String, ?> execute(RestContext restContext, IRestClient<Map<String, ?>, String> restClient) {
        String eTag = restContext.getEtagStrValue();
        String name = getName();
        return restClient.patch(restContext.getURI(name),
                restContext.getBodyString(name), eTag,
                restContext.getHeaders(name),
                restContext.getNamedJsonPathExpression(name));
    }
}

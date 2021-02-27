package com.lotuslabs.rest.model.actions;

import com.lotuslabs.rest.interfaces.IRestClient;
import com.lotuslabs.rest.model.RestContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public
class FormPostAction extends RestAction {
    public static final String NAME = "form";

    public FormPostAction(String name) {
        super(name);
    }

    @Override
    public Map<String, ?> execute(RestContext restContext,
                                  IRestClient<Map<String, ?>, String> restClient) {
        String name = getName();
        return restClient.formPost(restContext.getURI(name),
                restContext.getBodyString(name),
                restContext.getNamedJsonPathExpression(name));
    }
}
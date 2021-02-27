package com.lotuslabs.rest.model.actions;

import com.lotuslabs.rest.interfaces.IRestClient;
import com.lotuslabs.rest.model.RestContext;

import java.util.Map;

public class LoginAction extends RestAction {

    public static final String NAME = "login";

    public LoginAction() {
        super(NAME);
    }

    @Override
    public Map<String, ?> execute(RestContext restContext, IRestClient<Map<String, ?>, String> restClient) {
        String name = getName();
        return restClient.formPost(restContext.getURI(name),
                restContext.getBodyString(name),
                restContext.getAccessTokenJsonPathExpression() );
    }
}
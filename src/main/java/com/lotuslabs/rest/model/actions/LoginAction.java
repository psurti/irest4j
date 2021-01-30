package com.lotuslabs.rest.model.actions;

import com.lotuslabs.rest.interfaces.IConfig;
import com.lotuslabs.rest.interfaces.IRestClient;

import java.util.Map;

public class LoginAction extends RestAction {

    public static final String NAME = "login";

    public LoginAction(IConfig config) {
        super(NAME, config);
    }

    @Override
    public Map<String,?> execute(Map<String, Object> context,
                                 IRestClient<Map<String,?>, String> restClient) {
        return restClient.formPost(getURI(context), getBody().toString(), getAccessTokenJsonPathParam());
    }
}
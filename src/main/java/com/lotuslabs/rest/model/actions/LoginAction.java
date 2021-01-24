package com.lotuslabs.rest.model.actions;

import com.lotuslabs.rest.interfaces.IRestClient;
import com.lotuslabs.rest.model.JsonPathParam;

import java.util.Map;
import java.util.Properties;

class LoginAction extends RestAction {

    public static final String NAME = "login";
    private final JsonPathParam[] params;

    public LoginAction(Properties properties) {
        super(NAME, properties);
        params = new JsonPathParam[] {JsonPathParam.valueOf("bearer", "$.access_token")};
    }

    @Override
    public Map<String,?> execute(Map<String, Object> context,
                                 IRestClient<Map<String,?>, String> restClient) {
        return restClient.formPost(getURI(context), getBody().toString(), params);
    }
}
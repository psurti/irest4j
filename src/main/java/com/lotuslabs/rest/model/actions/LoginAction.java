package com.lotuslabs.rest.model.actions;

import com.lotuslabs.rest.interfaces.IConfig;
import com.lotuslabs.rest.interfaces.IRestClient;
import com.lotuslabs.rest.model.JsonPathParam;

import java.util.Map;

public class LoginAction extends RestAction {

    public static final String NAME = "login";
    private final JsonPathParam[] params;

    public LoginAction(IConfig config) {
        super(NAME, config);
        params = new JsonPathParam[] {JsonPathParam.valueOf("bearer", "$.access_token")};
    }

    @Override
    public Map<String,?> execute(Map<String, Object> context,
                                 IRestClient<Map<String,?>, String> restClient) {
        return restClient.formPost(getURI(context), getBody().toString(), params);
    }
}
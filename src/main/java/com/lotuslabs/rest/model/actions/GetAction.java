package com.lotuslabs.rest.model.actions;

import com.lotuslabs.rest.interfaces.IRestClient;

import java.util.Map;
import java.util.Properties;

class GetAction extends RestAction {
    public static final String NAME = "get";

    public GetAction(String name, Properties properties) {
        super(name, properties);
    }

    @Override
    public Map<String,?> execute(Map<String, Object> context,
                                 IRestClient<Map<String,?>, String> restClient) {
        return restClient.get(getURI(context), getJsonPathParams());
    }
}

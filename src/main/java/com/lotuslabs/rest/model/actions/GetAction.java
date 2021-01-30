package com.lotuslabs.rest.model.actions;

import com.lotuslabs.rest.interfaces.IConfig;
import com.lotuslabs.rest.interfaces.IRestClient;

import java.util.Map;

public class GetAction extends RestAction {
    public static final String NAME = "get";

    public GetAction(String name, IConfig config) {
        super(name, config);
    }

    @Override
    public Map<String,?> execute(Map<String, Object> context,
                                 IRestClient<Map<String,?>, String> restClient) {
        return restClient.get(getURI(context), getNamedJsonPathExpression());
    }
}

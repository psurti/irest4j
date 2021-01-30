package com.lotuslabs.rest.model.actions;

import com.lotuslabs.rest.interfaces.IConfig;
import com.lotuslabs.rest.interfaces.IRestClient;

import java.util.Map;

public class DeleteAction extends RestAction {
    public static final String NAME = "delete";

    public DeleteAction(String name, IConfig config) {
        super(name, config);
    }

    public Map<String,?> execute(Map<String, Object> context,
                                 IRestClient<Map<String,?>, String> restClient) {
        return restClient.delete(getURI(context), getNamedJsonPathExpression());
    }
}
package com.lotuslabs.rest.model.actions;

import com.lotuslabs.rest.interfaces.IConfig;
import com.lotuslabs.rest.interfaces.IRestClient;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public
class FormPostAction extends RestAction {
    public static final String NAME = "form";

    public FormPostAction(String name, IConfig config) {
        super(name, config);
    }

    @Override
    public Map<String,?> execute(Map<String, Object> context,
                                 IRestClient<Map<String,?>, String> restClient) {
        return restClient.formPost(getURI(context), getBody().toString(), getJsonPathParams());
    }
}
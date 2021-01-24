package com.lotuslabs.rest.model.actions;

import com.lotuslabs.rest.interfaces.IRestClient;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Properties;

@Slf4j
class FormPostAction extends RestAction {
    public static final String NAME = "form";

    public FormPostAction(String name, Properties properties) {
        super(name, properties);
    }

    @Override
    public Map<String,?> execute(Map<String, Object> context,
                                 IRestClient<Map<String,?>, String> restClient) {
        return restClient.formPost(getURI(context), getBody().toString(), getJsonPathParams());
    }
}
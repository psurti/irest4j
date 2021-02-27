package com.lotuslabs.rest.model.actions;

import com.lotuslabs.rest.interfaces.IRestClient;
import com.lotuslabs.rest.model.RestContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class PutAction extends RestAction {
    public static final String NAME = "put";

    public PutAction(String name) {
        super(name);
    }

    @Override
    public Map<String, ?> execute(RestContext restContext, IRestClient<Map<String, ?>, String> restClient) {
        String eTag = getETag(restContext);
        String name = getName();
        return restClient.put(restContext.getURI(name),
                restContext.getBodyString(name), eTag,
                restContext.getNamedJsonPathExpression(name));
    }

    private String getETag(RestContext restContext) {
        String eTag = null;
        final List<Object> valueList = restContext.getEtag();
        if ( !valueList.isEmpty() ) {
            final Object o = valueList.get(0);
            if (o != null) {
                eTag = o.toString();
                eTag = eTag.substring(1, eTag.length() - 1); //hack
                log.warn( "etag:{}", eTag);
            }
        }
        return eTag;
    }
}

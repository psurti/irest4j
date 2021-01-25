package com.lotuslabs.rest.interfaces;

import com.lotuslabs.rest.model.JsonPathParam;
import com.lotuslabs.rest.model.actions.RestAction;

import java.util.Map;

public interface IConfig {
    String getHost();
    String getBearer();
    String getBasicAuth();
    String getConsulToken();
    boolean isPretty();
    Map<String,String> getContext();
    RestAction[] getActions();
    String getPath(String name);
    boolean isEncodeUrl(String name);
    boolean isEncodePath(String name);
    String getBody(String name);
    JsonPathParam[] getJsonExps(String name);
}
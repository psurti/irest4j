package com.lotuslabs.rest.interfaces;

import com.lotuslabs.rest.model.NamedJsonPathExpression;
import com.lotuslabs.rest.model.actions.RestAction;

import java.util.Map;

public interface IConfig {
    String getHost(String name);
    String getHost();
    String getBearer();
    String getBasicAuth();
    String getConsulToken();
    boolean isPretty();
    OutputListener getResultListener(Result result);
    Map<String,String> getContext();
    RestAction[] getActions();
    String getPath(String name);
    boolean isEncodeUrl(String name);
    boolean isEncodePath(String name);
    String getBody(String name);
    Map<String, NamedJsonPathExpression> getJsonExps(String name);
    String getName();
    boolean ignoreFailure(String name);
}

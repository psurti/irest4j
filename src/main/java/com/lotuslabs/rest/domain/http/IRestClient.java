package com.lotuslabs.rest.domain.http;

import com.lotuslabs.rest.model.NamedJsonPathExpression;

import java.net.URI;
import java.util.Map;

public interface IRestClient<R, P> {
    R delete(URI finalUri, Map<String,String> headers, NamedJsonPathExpression... namedJsonPathExpressions);

    R formPost(URI finalUri, P body, Map<String,String> headers, NamedJsonPathExpression... namedJsonPathExpressions);

    R post(URI finalUri, P body,  Map<String,String> headers, NamedJsonPathExpression... namedJsonPathExpressions);

    R put(URI finalUri, P body, String eTag,  Map<String,String> headers, NamedJsonPathExpression... namedJsonPathExpressions);

    R get(URI finalUri,  Map<String,String> headers, NamedJsonPathExpression... namedJsonPathExpressions);

    R patch(URI uri, P bodyString, P eTag,  Map<String,String> headers, NamedJsonPathExpression[] namedJsonPathExpression);
}

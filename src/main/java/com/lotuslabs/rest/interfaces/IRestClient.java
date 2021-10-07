package com.lotuslabs.rest.interfaces;

import com.lotuslabs.rest.model.NamedJsonPathExpression;

import java.net.URI;

public interface IRestClient<R, P> {
    R delete(URI finalUri, NamedJsonPathExpression... namedJsonPathExpressions);

    R formPost(URI finalUri, P body, NamedJsonPathExpression... namedJsonPathExpressions);

    R post(URI finalUri, P body, NamedJsonPathExpression... namedJsonPathExpressions);

    R put(URI finalUri, P body, String eTag, NamedJsonPathExpression... namedJsonPathExpressions);

    R get(URI finalUri, NamedJsonPathExpression... namedJsonPathExpressions);

    R patch(URI uri, P bodyString, P eTag, NamedJsonPathExpression[] namedJsonPathExpression);
}

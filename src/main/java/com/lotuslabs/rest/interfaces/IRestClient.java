package com.lotuslabs.rest.interfaces;

import com.lotuslabs.rest.model.JsonPathParam;

import java.net.URI;

public interface IRestClient<R, P> {
    R delete(URI finalUri, JsonPathParam... jsonPathParams);
    R formPost(URI finalUri, P body, JsonPathParam... jsonPathParams);
    R post(URI finalUri, P body, JsonPathParam... jsonPathParams);
    R put(URI finalUri, P body, String eTag, JsonPathParam... jsonPathParams);
    R get(URI finalUri, JsonPathParam... jsonPathParams);
}

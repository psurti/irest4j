package com.lotuslabs.rest.adapters.config.http;

import com.lotuslabs.rest.domain.configuration.Configurable;
import com.lotuslabs.rest.domain.variables.VariableContext;
import com.lotuslabs.rest.domain.variables.VariableSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class RequestEntityFactory {


    private static VariableSet createRequestVariableSet(VariableContext requestContext,
                                                        Configurable configurable, String name) {
        final VariableSet set = VariableSet.create(configurable.getRequestHeaders(name));
        set.addVariable(configurable.getAbsUrl(name));
        set.addVariable(configurable.getRequestBody(name));
        set.resolveValues(requestContext);
        return set;
    }

    public static RequestEntity<Object> createPatchRequestEntity(VariableContext requestContext,
                                                                 String name, Configurable configurable) {
        final VariableSet variableSet = createRequestVariableSet(requestContext, configurable, name);
        final RequestEntity.BodyBuilder patch = RequestEntity.patch(variableSet.getVariable("uri").value());
        return createBodyBuilderRequestEntity(configurable.getRequestHeaders(name), patch, variableSet);
    }

    public static RequestEntity<Object> createPostRequestEntity(VariableContext requestContext, String name,
                                                                Configurable configurable) {
        final VariableSet variableSet = createRequestVariableSet(requestContext, configurable, name);
        final RequestEntity.BodyBuilder post = RequestEntity.post(variableSet.getVariable("uri").value());
        return createBodyBuilderRequestEntity( configurable.getRequestHeaders(name), post, variableSet);
    }

    public static RequestEntity.HeadersBuilder<?> createDeleteRequestEntity(VariableContext requestContext,
                                                                            String name, Configurable configurable) {
        final VariableSet variableSet = createRequestVariableSet(requestContext, configurable, name);
        final RequestEntity.HeadersBuilder<?> delete = RequestEntity.delete(variableSet.getVariable("uri").value());
        return getHeadersBuilder( configurable.getRequestHeaders(name), delete);
    }


    public static RequestEntity<Object> createPutRequestEntity(VariableContext requestContext,
                                                               String name, Configurable configurable) {
        final VariableSet variableSet = createRequestVariableSet(requestContext, configurable, name);
        final RequestEntity.BodyBuilder put = RequestEntity.put(variableSet.getVariable("uri").value());
        return createBodyBuilderRequestEntity( configurable.getRequestHeaders(name), put, variableSet);
    }


    public static RequestEntity<Object> createFormPostRequestEntity(VariableContext requestContext,
                                                                    String name, Configurable configurable) {
        final VariableSet variableSet = createRequestVariableSet(requestContext, configurable, name);

        final RequestEntity.BodyBuilder request = RequestEntity.post(variableSet.getVariable("uri").value());
        final Map<String,String> defaultHeaders = defaultHeaders();
        //overwrite the default content-type for form.
        defaultHeaders.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        applyHeaders(configurable.getRequestHeaders(name), request, defaultHeaders);
        final String body = variableSet.getVariable("body").value();
        return request.body(getFormBody(body));
    }

    public static RequestEntity.HeadersBuilder<?> createGet(VariableContext requestContext,
                                                            String name, Configurable configurable) {
        final VariableSet variableSet = createRequestVariableSet(requestContext, configurable, name);
        final Map<String, String> requestHeaders = configurable.getRequestHeaders(name);
        final String uri = variableSet.getVariable("url").value();
        final RequestEntity.HeadersBuilder<?> request = RequestEntity.get(uri);
        return getHeadersBuilder(requestHeaders, request);

    }

    private static RequestEntity<Object> createBodyBuilderRequestEntity(
            final Map<String,String> headers,final RequestEntity.BodyBuilder builder,
            final VariableSet variableSet) {
        final String body = variableSet.getVariable("body").value();
        log.debug("Body length = {}", body.length() );
        applyHeaders(headers, builder, defaultHeaders());
        return builder.body(body);
    }


    private static Object getFormBody(String body) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        final String[] pairs = body.split("&");
        for (String pair : pairs) {
            final String[] nv = pair.trim().split("=");
            log.debug(nv[0] + " " + nv[1]);
            params.add(nv[0].trim(), nv[1].trim());
        }
        return params;
    }


    private static RequestEntity.HeadersBuilder<?> getHeadersBuilder(Map<String, String> headers,
                                                                     RequestEntity.HeadersBuilder<?> request) {
        final Map<String, String> defaultHeaders = defaultHeaders();
        defaultHeaders.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE + "," + "*/*");
        applyHeaders(headers, request, defaultHeaders);
        return request;
    }

    private static Map<String,String> defaultHeaders() {
        Map<String,String> defaultHeaders = new LinkedHashMap<>();
        defaultHeaders.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        defaultHeaders.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return defaultHeaders;
    }

    private static void applyHeaders(final Map<String, String> headers, RequestEntity.HeadersBuilder<?> request,
                                     final Map<String, String> defaultHeaders) {

        final Map<String,String> mutableDefaultHeaders = new LinkedHashMap<>(defaultHeaders);
        headers.forEach( (k,v) -> {
            // if value is empty/null then do not add default header for it.
            if (v == null || v.trim().length() == 0) {
                mutableDefaultHeaders.remove(k);
            } else {
                request.header(k, v.split(","));
            }
        });

        mutableDefaultHeaders.forEach( (k, v) -> {
            if (!headers.containsKey(k) && v != null) {
                request.header(k, v.split(","));
            }
        });
        request.acceptCharset(StandardCharsets.UTF_8);
    }

}


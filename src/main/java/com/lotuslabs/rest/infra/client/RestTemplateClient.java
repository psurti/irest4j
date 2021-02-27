package com.lotuslabs.rest.infra.client;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.internal.JsonFormatter;
import com.lotuslabs.rest.interfaces.IConfig;
import com.lotuslabs.rest.interfaces.IRestClient;
import com.lotuslabs.rest.model.NamedJsonPathExpression;
import com.lotuslabs.rest.model.RestContext;
import com.lotuslabs.rest.model.actions.RestAction;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONValue;
import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author prsurt
 * @version 9/4/2020 5:04 PM
 */
@Slf4j
public class RestTemplateClient implements IRestClient<Map<String,?>, String> {
    private final Boolean pretty;
    private String bearer;
    private final String basic;
    private final String consulToken;
    private final RestTemplate restTemplate;
    public static final String X_CONSUL_TOKEN = "X-Consul-Token";
    public static final String X_CSRF_TOKEN = "X-CSRF-TOKEN";
    public static final String X_CSRF_HEADER = "X-CSRF-HEADER";
    private String xCsrfToken;
    private final RestContext restContext;

    public RestTemplateClient(IConfig config) {
        this.bearer = config.getBearer();
        this.basic = config.getBasicAuth();
        this.consulToken = config.getConsulToken();
        this.pretty = config.isPretty();
        this.restContext = new RestContext(config);
        this.restTemplate = new RestTemplate();
        configureMessageConverters();
        configureClientHttpRequestFactory();
    }

    private void configureClientHttpRequestFactory() {
        final SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(10_000);
        clientHttpRequestFactory.setReadTimeout(30_000);
        restTemplate.setRequestFactory(clientHttpRequestFactory);
    }

    private void configureMessageConverters() {
        final List<HttpMessageConverter<?>> converters = new ArrayList<>(restTemplate.getMessageConverters());
        final FormHttpMessageConverter converter = new FormHttpMessageConverter();
        converter.setSupportedMediaTypes(Collections.singletonList( MediaType.APPLICATION_FORM_URLENCODED));
        converters.add(converter);
        restTemplate.setMessageConverters(converters);
    }

    private String getAuthorization() {
        String ret = null;
        if (basic != null) {
            ret = "Basic " + (Base64.isBase64(basic) ? basic : Base64.encodeBase64String(basic.getBytes()));
        }
        if (bearer != null) {
            ret = "bearer " + bearer;
        }
        return ret;
    }

    public RequestEntity.HeadersBuilder<?> createGetRequestEntity(URI finalUri) {
        return RequestEntity.get(finalUri)
                .header(HttpHeaders.AUTHORIZATION, getAuthorization())
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE, "*/*")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(X_CONSUL_TOKEN, this.consulToken)
                .acceptCharset(StandardCharsets.UTF_8);
    }

    public RequestEntity<Object> createPutRequestEntity(URI finalUri, Object body, String eTag) {
        final RequestEntity.BodyBuilder put = RequestEntity.put(finalUri);
        if (eTag != null) {
            put.header(HttpHeaders.IF_MATCH, eTag);
        }
        return createBodyBuilderRequestEntity(put, body);
    }

    public RequestEntity<Object> createPostRequestEntity(URI finalUri, Object body) {
        return createBodyBuilderRequestEntity(RequestEntity.post(finalUri), body);
    }

    public RequestEntity.HeadersBuilder<?> createDeleteRequestEntity(URI finalUri) {
        return RequestEntity.delete(finalUri)
                .header(HttpHeaders.AUTHORIZATION, getAuthorization())
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE, "*/*")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(X_CONSUL_TOKEN, this.consulToken)
                .acceptCharset(StandardCharsets.UTF_8);
    }

    private RequestEntity<Object> createBodyBuilderRequestEntity(RequestEntity.BodyBuilder builder, Object body) {
        String csrf = (xCsrfToken == null) ? "" : xCsrfToken;
        return builder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(X_CSRF_HEADER, X_CSRF_TOKEN)
                .header(X_CSRF_TOKEN, csrf)
                .header(HttpHeaders.AUTHORIZATION, getAuthorization())
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(X_CONSUL_TOKEN, this.consulToken)
                .acceptCharset(StandardCharsets.UTF_8)
                .body(body);
    }

    public RequestEntity<Object> createFormPostRequestEntity(URI finalUri, String body) {
        String csrf = (xCsrfToken == null) ? "" : xCsrfToken;
        return RequestEntity.post(finalUri)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .header(X_CSRF_HEADER, X_CSRF_TOKEN)
                .header(X_CSRF_TOKEN, csrf)
                .header(HttpHeaders.AUTHORIZATION, getAuthorization())
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(X_CONSUL_TOKEN, this.consulToken)
                .acceptCharset(StandardCharsets.UTF_8)
                .body(getFormBody(body));
    }

    private Object getFormBody(String body) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        final String[] pairs = body.split("&");
        for (String pair : pairs) {
            final String[] nv = pair.trim().split("=");
            log.debug(nv[0] + " " + nv[1]);
            params.add(nv[0].trim(), nv[1].trim());
        }
        return params;
    }

    public void execute(RestAction... actions) throws IOException {
        for (RestAction action : actions) {
            restContext.updateSequenceId();
            Map<String, ?> results = action.execute(restContext,this);
            if (results != null){
                restContext.updateContext(results);
                Object val  = results.get("bearer");
                if (val != null) {
                    bearer = val.toString();
                }
            }
        }
    }

    public <T> ResponseEntity<T> exchange(RequestEntity<?> requestEntity, Class<T> responseType)  {
        log.debug("\n=================================================");
        log.debug("{}", requestEntity);
        final ResponseEntity<T> responseEntity = restTemplate.exchange(requestEntity, responseType);
        log.debug("\n-------------------------------------------------");
        log.debug("Status:{}",responseEntity.getStatusCode());
        log.debug("Headers:{}",responseEntity.getHeaders());
        final String xCsrfTokenVal = responseEntity.getHeaders().getFirst(X_CSRF_TOKEN);
        if (!Objects.equals(this.xCsrfToken, xCsrfTokenVal)) {
            this.xCsrfToken = xCsrfTokenVal;
        }
        log.debug("\n=================================================");
        return responseEntity;
    }


    @SuppressWarnings("unchecked")
    public Map<String, Object> parseJsonStringToMap(ResponseEntity<String> responseEntity, NamedJsonPathExpression... params) {
        log.debug("{}", (Boolean.TRUE.equals(pretty) && responseEntity.getBody() != null) ?
                JsonFormatter.prettyPrint(responseEntity.getBody()) :
                responseEntity.getBody());
        final HttpHeaders headers = responseEntity.getHeaders();
        final LinkedHashMap<String, Object> ret = new LinkedHashMap<>();
        if (responseEntity.getBody() != null) {
            for (NamedJsonPathExpression param : params) {
                String responseBody = responseEntity.getBody();
                Object val = JsonPath.parse(responseBody).read(param.getJsonPath());
                if (val != null) {
                    if (param.getJsonPathLabel() != null
                            && val instanceof List
                            &&  ((List<?>) val).size()==1) {
                        //Flatten array of 1
                        val = ((List<?>) val).get(0);
                    }
                    String strVal = String.valueOf(val);
                    if (!param.checkValue(strVal)) {
                        log.warn("#{} Actual:{} Expected:{}", param.getJsonPathLabel(), strVal, param.getExpectedValue() );
                    }
                    ret.put(param.getJsonPathLabel(), strVal);
                }
            }
        }
        if (!ret.isEmpty()) {
            for(Map.Entry<String,Object> entry: ret.entrySet()) {
                log.info("#{}={}", entry.getKey(), entry.getValue());
            }
        } else {
            final Object val = JSONValue.parseKeepingOrder(responseEntity.getBody());
            if (val instanceof List) {
                ret.put(".", val);
            } else if (val instanceof Map ) {
                ret.putAll((Map<? extends String, ?>) val);
            } else {
                throw new IllegalArgumentException("unsupported type " +  val);
            }
        }
        ret.putAll(headers);
        return ret;
    }


    @Override
    public Map<String, ?> delete(URI finalUri, NamedJsonPathExpression... namedJsonPathExpressions) {
        final ResponseEntity<String> responseEntity = exchange(
                createDeleteRequestEntity(finalUri).build(),
                String.class);
        return parseJsonStringToMap(responseEntity, namedJsonPathExpressions);
    }

    @Override
    public Map<String, ?> formPost(URI finalUri, String body, NamedJsonPathExpression... namedJsonPathExpressions) {
        final RequestEntity<?> formPostRequestEntity = createFormPostRequestEntity(finalUri, body);
        final ResponseEntity<String> responseEntity = exchange(formPostRequestEntity, String.class);
        return parseJsonStringToMap( responseEntity, namedJsonPathExpressions);
    }

    @Override
    public Map<String, ?> put(URI finalUri, String body, String eTag, NamedJsonPathExpression... namedJsonPathExpressions) {
        final ResponseEntity<String> responseEntity = exchange(
                createPutRequestEntity(finalUri, body, eTag),
                String.class);
        return parseJsonStringToMap(responseEntity, namedJsonPathExpressions);
    }

    @Override
    public  Map<String, ?> get(URI finalUri, NamedJsonPathExpression... namedJsonPathExpressions) {
        final ResponseEntity<String> responseEntity = exchange(
                createGetRequestEntity(finalUri).build(),
                String.class);
        return parseJsonStringToMap(responseEntity, namedJsonPathExpressions);
    }

    @Override
    public Map<String, ?> post(URI finalUri, String body, NamedJsonPathExpression... namedJsonPathExpressions) {
        final ResponseEntity<String> responseEntity = exchange(
                createPostRequestEntity(finalUri, body), String.class);
        return parseJsonStringToMap(responseEntity, namedJsonPathExpressions);
    }
}

package com.lotuslabs.rest.adapters.http;

import com.jayway.jsonpath.internal.JsonFormatter;
import com.lotuslabs.rest.domain.configuration.Configurable;
import com.lotuslabs.rest.model.NamedJsonPathExpression;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONValue;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * @author prsurt
 * @version 9/4/2020 5:04 PM
 */
@Slf4j
public class RestTemplateClient  {
    private final RestTemplate restTemplate;

    public RestTemplateClient(Configurable config) {
        this.restTemplate = new RestTemplate();
        configureMessageConverters();
        configureClientHttpRequestFactory();
    }

    private void configureClientHttpRequestFactory() {
        //final SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        clientHttpRequestFactory.setConnectTimeout(0);
        clientHttpRequestFactory.setReadTimeout(0);
        restTemplate.setRequestFactory(clientHttpRequestFactory);
    }

    private void configureMessageConverters() {
        final List<HttpMessageConverter<?>> converters = new ArrayList<>(restTemplate.getMessageConverters());
        final FormHttpMessageConverter converter = new FormHttpMessageConverter();
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_FORM_URLENCODED));
        converters.add(converter);
        restTemplate.setMessageConverters(converters);
    }


    public <T> ResponseEntity<T> exchange(RequestEntity<?> requestEntity, Class<T> responseType) {
        log.debug("\n=================================================");
        log.debug("{}", requestEntity);
        final ResponseEntity<T> responseEntity = restTemplate.exchange(requestEntity, responseType);
        log.debug("\n-------------------------------------------------");
        log.debug("Status:{}", responseEntity.getStatusCode());
        log.debug("Headers:{}", responseEntity.getHeaders());
        log.debug("\n=================================================");
        return responseEntity;
    }


    @SuppressWarnings("unchecked")
    public Map<String, Object> evaluate(ResponseEntity<String> responseEntity, NamedJsonPathExpression... params) {
        log.debug("{}", (responseEntity.getBody() != null) ? JsonFormatter.prettyPrint(responseEntity.getBody()) :
                responseEntity.getBody());
        final HttpHeaders headers = responseEntity.getHeaders();
        final LinkedHashMap<String, Object> ret = new LinkedHashMap<>();
        final Object val = JSONValue.parseKeepingOrder(responseEntity.getBody());
        if (val instanceof List) {
            ret.put(".", val);
        } else if (val instanceof Map) {
            ret.putAll((Map<? extends String, ?>) val);
        } else if (val != null) {
            throw new IllegalArgumentException("unsupported type " + val);
        }
        ret.putAll(headers);
        return ret;
    }
}

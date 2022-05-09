package com.lotuslabs.rest.adapters.http;

import com.lotuslabs.rest.domain.configuration.Configurable;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

}

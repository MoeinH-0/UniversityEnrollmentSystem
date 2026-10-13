package com.example.universityenrollmentsystem.e2e;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class TestRestClient {

    private static final String HTTP_SCHEME = "http://";
    private static final String HTTPS_SCHEME = "https://";
    private static final String LOCALHOST_PREFIX = "http://localhost:";

    private final RestTemplate restTemplate;
    private final int port;

    public TestRestClient(int port) {
        this.port = port;
        this.restTemplate = new RestTemplate();
        this.restTemplate.setErrorHandler(response -> false);
    }

    private String resolveUrl(String url) {
        if (url.startsWith(HTTP_SCHEME) || url.startsWith(HTTPS_SCHEME))
            return url;

        return LOCALHOST_PREFIX + port + url;
    }

    public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType) {
        return restTemplate.postForEntity(resolveUrl(url), request, responseType);
    }

    public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType) {
        return restTemplate.getForEntity(resolveUrl(url), responseType);
    }

    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType) {
        return restTemplate.exchange(resolveUrl(url), method, requestEntity, responseType);
    }
}

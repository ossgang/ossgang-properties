package io.gihub.ossgang.properties.web.client;

import io.gihub.ossgang.properties.web.client.rbac.RbacTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

public class RestClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestClient.class);
    private static final String X_RBAC_TOKEN_HEADER = "X-RBAC-TOKEN";

    @Autowired
    private RbacTokenProvider rbacTokenProvider;

    private final String baseUrl;

    public RestClient(String baseUrl) {
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl must not be null");
    }

    public Optional<ResponseEntity<Void>> doGet(RemoteEndpoint endpoint) {
        return doGet(endpoint, Void.class);
    }

    public <T> Optional<ResponseEntity<T>> doGet(RemoteEndpoint endpoint, Class<T> responseType) {
        return doRest(endpoint, Collections.emptyMap(), responseType, HttpMethod.GET);
    }

    public Optional<ResponseEntity<Void>> doPost(RemoteEndpoint endpoint) {
        return doRest(endpoint, Collections.emptyMap(), Void.class, HttpMethod.POST);
    }

    public <T> Optional<ResponseEntity<T>> doPost(RemoteEndpoint endpoint, Class<T> responseType) {
        return doRest(endpoint, Collections.emptyMap(), responseType, HttpMethod.POST);
    }

    public <T> Optional<ResponseEntity<T>> doPost(RemoteEndpoint endpoint, Map<String, String> payload,
            Class<T> responseType) {
        return doRest(endpoint, payload, responseType, HttpMethod.POST);
    }

    public Optional<ResponseEntity<Void>> doPost(RemoteEndpoint endpoint, Map<String, String> payload) {
        return doRest(endpoint, payload, Void.class, HttpMethod.POST);
    }

    public <T> Optional<ResponseEntity<T>> doRest(RemoteEndpoint endpoint, Map<String, String> payload,
            Class<T> responseType, HttpMethod httpMethod) {
        RestTemplate restTemplate = new RestTemplate();
        String url = composeRemoteUrl(endpoint);
        HttpEntity<MultiValueMap<String, String>> request = defaultRequest(payload);
        try {
            ResponseEntity<T> response = restTemplate.exchange(url, httpMethod, request, responseType);

            if (response.getStatusCode().is2xxSuccessful()) {
                LOGGER.debug("{} ({}) command successfully sent", endpoint.name(), baseUrl);
                return Optional.of(response);
            }

        } catch (HttpStatusCodeException e) {
            LOGGER.error("{} ({}) error. HTTP code {}. Response: {}", endpoint.name(), baseUrl, e.getStatusCode(),
                    e.getResponseBodyAsString());
            return Optional.of(new ResponseEntity<>(e.getStatusCode()));
        } catch (Exception e) {
            LOGGER.error("{} ({}) error contacting server: {}", endpoint.name(), baseUrl, e.getMessage(), e);
        }
        return Optional.empty();
    }

    public String getRemoteHost() {
        return baseUrl;
    }

    private String composeRemoteUrl(RemoteEndpoint endpoint) {
        return baseUrl + endpoint.relativeUrl();
    }

    private HttpEntity<MultiValueMap<String, String>> defaultRequest(Map<String, String> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_RBAC_TOKEN_HEADER, Base64.getEncoder().encodeToString(rbacTokenProvider.getRbacToken()));
        if (!payload.isEmpty()) {
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        }
        MultiValueMap<String, String> multivalueMapForHttp = payload.entrySet().stream() //
                .collect(collectingAndThen( //
                        toMap(Map.Entry::getKey, e -> singletonList(e.getValue())), LinkedMultiValueMap::new));
        return new HttpEntity<>(multivalueMapForHttp, headers);
    }

}

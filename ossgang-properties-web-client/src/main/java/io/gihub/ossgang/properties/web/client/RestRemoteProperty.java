/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package io.gihub.ossgang.properties.web.client;

import com.google.common.collect.ImmutableMap;
import io.github.ossgang.properties.core.Property;
import io.github.ossgang.properties.core.Source;
import io.github.ossgang.properties.core.Sources;
import io.github.ossgang.properties.core.ValueConversions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;


import static io.gihub.ossgang.properties.web.client.RestUtils.isSuccessfulResponse;
import static io.gihub.ossgang.properties.web.client.WebSocketFlux.fluxFrom;
import static io.github.ossgang.properties.core.ValueConversions.stringToValue;
import static io.github.ossgang.properties.core.ValueConversions.valueToString;

public class RestRemoteProperty<T> implements Property<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestRemoteProperty.class);

    private final RemoteEndpoint endpoint;
    private final Type type;
    private final AtomicReference<T> latestValue = new AtomicReference<>();
    private final EmitterProcessor<T> localSink = EmitterProcessor.create();
    private Flux<T> stream;

    private static final String PROPERTY_ENDPOINT = "/properties/";
    private static final String PROPERTY_WEBSOCKET_ENDPOINT = "/properties/ws/";

    private final RestClient restClient;
    private final String webSocketBaseUrl;

    public RestRemoteProperty(RestClient restClient, String webSocketBaseUrl, Type type, String endpoint) {
        this(restClient, webSocketBaseUrl, type, () -> endpoint);
    }

    public RestRemoteProperty(RestClient restClient, String webSocketBaseUrl, Type type, RemoteEndpoint endpoint) {
        this.type = type;
        this.endpoint = endpoint;
        this.restClient = restClient;
        this.webSocketBaseUrl = webSocketBaseUrl;
        initStream();
    }

    private String relativeEndpointUrl() {
        return PROPERTY_ENDPOINT + endpoint.relativeUrl();
    }

    private String relativeEndpointWebsocketUrl() {
        return PROPERTY_WEBSOCKET_ENDPOINT + endpoint.relativeUrl();
    }

    private void initStream() {
        Flux<T> remoteStream = fluxFrom(webSocketBaseUrl + relativeEndpointWebsocketUrl())
                .map(v -> (T) stringToValue(type, v)).cache(1);
        stream = Flux.merge(remoteStream, localSink).publishOn(Schedulers.elastic())
                .onBackpressureDrop(v -> LOGGER.warn("{}: dropped due to backpressure: {}", endpoint.relativeUrl(), v));
        stream.subscribe(latestValue::set);
    }

    @Override
    public T get() {
        T value = stringToValue(type, restClient.doGet(this::relativeEndpointUrl, String.class).get().getBody());
        latestValue.set(value);
        return value;
    }

    @Override
    public void set(T value) {
        T lastValue = latestValue.get();
        if (Objects.equals(value, lastValue)) {
            LOGGER.debug("{}: not setting value {} on the server, equal to the latest value!", endpoint.relativeUrl(), value);
        } else {
            ImmutableMap<String, String> parameters = ImmutableMap.of("value", valueToString(value));
            Optional<ResponseEntity<String>> response = restClient.doPost(this::relativeEndpointUrl, parameters, String.class);
            if (!isSuccessfulResponse(response)) {
                LOGGER.trace("{}: error from server, publishing old value downstream", endpoint.relativeUrl());
                localSink.onNext(lastValue);
            }
        }
    }

    @Override
    public Source<T> getSource() {
        return Sources.sourceFrom(stream);
    }

}

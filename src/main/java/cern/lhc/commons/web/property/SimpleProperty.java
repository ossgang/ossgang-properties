/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.lhc.commons.web.property;

import reactor.core.publisher.Flux;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class SimpleProperty<T> implements Property<T> {
    private final AtomicReference<T> latestValue = new AtomicReference<>();
    private final Sink<T> sink = Sink.createSink();

    SimpleProperty(T initialValue) {
        if (initialValue != null) {
            set(initialValue);
        }
    }

    @Override
    public T get() {
        return latestValue.get();
    }

    @Override
    public void set(T value) {
        if (!Objects.equals(latestValue.getAndSet(value), value)) {
            sink.push(value);
        }
    }

    @Override
    public Flux<T> asStream() {
        return sink.asStream();
    }

}

/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.lhc.commons.web.property;

import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static reactor.core.scheduler.Schedulers.elastic;

public class SimpleProperty<T> implements Property<T> {
    private final AtomicReference<T> latestValue = new AtomicReference<>();
    private final ReplayProcessor<T> updateStream = ReplayProcessor.cacheLast();
    private final Class<? extends T> valueClass;

    SimpleProperty(Class<? extends T> valueClass, T initialValue) {
        this.valueClass = valueClass;
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
        if(!Objects.equals(latestValue.getAndSet(value), value)) {
            updateStream.onNext(value);
        }
    }

    @Override
    public Flux<T> asStream() {
        return updateStream.publishOn(elastic()).onBackpressureDrop();
    }

    @Override
    public Class<? extends T> valueClass() {
        return valueClass;
    }
}

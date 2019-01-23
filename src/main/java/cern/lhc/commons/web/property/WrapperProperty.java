/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.lhc.commons.web.property;

import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static reactor.core.scheduler.Schedulers.elastic;

public class WrapperProperty<T> implements Property<T> {
    private final AtomicReference<T> latestValue = new AtomicReference<>();
    private final Flux<T> updateStream;
    private final Consumer<T> consumer;

    WrapperProperty(Flux<T> stream, Consumer<T> consumer) {
        this.updateStream = stream;
        this.consumer = consumer;
        stream.subscribe(latestValue::set);
    }

    @Override
    public T get() {
        return latestValue.get();
    }

    @Override
    public void set(T value) {
        consumer.accept(value);
    }

    @Override
    public Flux<T> asStream() {
        return updateStream.publishOn(elastic()).onBackpressureDrop();
    }

}

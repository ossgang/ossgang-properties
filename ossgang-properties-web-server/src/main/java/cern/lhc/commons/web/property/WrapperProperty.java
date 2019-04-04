/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.lhc.commons.web.property;

import io.github.ossgang.properties.core.Property;
import io.github.ossgang.properties.core.Source;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class WrapperProperty<T> implements Property<T> {
    private final AtomicReference<T> latestValue = new AtomicReference<>();
    private final Source<T> updateStream;
    private final Consumer<T> consumer;

    WrapperProperty(Flux<T> stream, Consumer<T> consumer) {
        this.updateStream = Sources.sourceFrom(stream);
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
    public Source<T> getSource() {
        return updateStream;
    }

}

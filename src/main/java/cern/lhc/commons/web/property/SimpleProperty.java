/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.lhc.commons.web.property;

import java.util.Objects;

public class SimpleProperty<T> implements Property<T> {
    private final Sink<T> sink = Sinks.createSink();

    SimpleProperty() {
        /* null initial value */
    }

    SimpleProperty(T initialValue) {
        if (initialValue != null) {
            set(initialValue);
        }
    }

    @Override
    public T get() {
        return sink.getLatest();
    }

    @Override
    public void set(T value) {
        synchronized (sink) {
            if (!Objects.equals(sink.getLatest(), value)) {
                sink.push(value);
            }
        }
    }

    @Override
    public Sink<T> getSource() {
        return sink;
    }

}

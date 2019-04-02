/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.lhc.commons.web.property;

import reactor.core.publisher.Flux;

public interface Property<T> {

    T get();

    void set(T value);

    Source<T> getSource();

    @Deprecated
    default Flux<T> asStream() {
        return getSource().asStream();
    }
}

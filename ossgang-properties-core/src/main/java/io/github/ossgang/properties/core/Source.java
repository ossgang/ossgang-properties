package io.github.ossgang.properties.core;

import reactor.core.publisher.Flux;

public interface Source<T> {

    T getLatest();

    Flux<T> asStream();

}

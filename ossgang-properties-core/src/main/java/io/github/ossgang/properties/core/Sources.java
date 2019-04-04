package io.github.ossgang.properties.core;

import io.github.ossgang.properties.core.StreamBasedSource;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public final class Sources {

    private static final Scheduler DEFAULT_SCHEDULER = Schedulers.elastic();

    public static <T> StreamBasedSource<T> sourceFrom(Flux<T> backingStream) {
        return new StreamBasedSource<>(backingStream, DEFAULT_SCHEDULER);
    }

    private Sources() {
        /* static stuff */
    }

}

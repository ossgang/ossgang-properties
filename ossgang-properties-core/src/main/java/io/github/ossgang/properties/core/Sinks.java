package io.github.ossgang.properties.core;

import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Scheduler;

public final class Sinks {

    public static <T> Sink<T> createSink() {
        return new Sink<>(ReplayProcessor.cacheLast());
    }

    public static <T> Sink<T> createSinkPublishingOn(Scheduler scheduler) {
        return new Sink<>(ReplayProcessor.cacheLast(), scheduler);
    }

    private Sinks() {
        /* static stuff */
    }

}

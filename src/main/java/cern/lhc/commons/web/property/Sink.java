package cern.lhc.commons.web.property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class Sink<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Sink.class);
    private static final Scheduler DEFAULT_SCHEDULER = Schedulers.elastic();

    private final ReplayProcessor<T> sink;
    private final Flux<T> stream;

    private Sink(Scheduler scheduler) {
        sink = ReplayProcessor.cacheLast();
        stream = sink.publishOn(scheduler)
                .onBackpressureDrop(v -> LOGGER.warn("Sink had to drop value due to backpressure {}", v));
    }

    public static <T> Sink<T> createSink() {
        return new Sink<>(DEFAULT_SCHEDULER);
    }

    public static <T> Sink<T> createSinkPublishingOn(Scheduler scheduler) {
        return new Sink<>(scheduler);
    }

    public void push(T value) {
        sink.onNext(value);
    }

    public Flux<T> asStream() {
        return stream;
    }

}

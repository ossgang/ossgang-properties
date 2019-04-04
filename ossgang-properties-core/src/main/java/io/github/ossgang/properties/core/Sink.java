package io.github.ossgang.properties.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.atomic.AtomicReference;

public class Sink<T> extends StreamBasedSource<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Sink.class);
    private static final Scheduler DEFAULT_SCHEDULER = Schedulers.elastic();

    private final ReplayProcessor<T> sink;
    private final Flux<T> stream;
    private final AtomicReference<T> latestValue = new AtomicReference<>();

    Sink(ReplayProcessor<T> replayProcessor) {
        this(replayProcessor, DEFAULT_SCHEDULER);
    }

    Sink(ReplayProcessor<T> replayProcessor, Scheduler scheduler) {
        super(replayProcessor, scheduler, null);
        sink = replayProcessor;
        stream = sink.publishOn(scheduler)
                .onBackpressureDrop(v -> LOGGER.warn("Sink had to drop value due to backpressure {}", v));
    }

    public void push(T value) {
        synchronized (sink) {
            latestValue.set(value);
            sink.onNext(value);
        }
    }

    @Override
    public T getLatest() {
        return latestValue.get();
    }

    @Override
    public Flux<T> asStream() {
        return stream;
    }

}

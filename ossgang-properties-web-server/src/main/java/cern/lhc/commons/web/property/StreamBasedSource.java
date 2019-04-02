package cern.lhc.commons.web.property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

import java.util.concurrent.atomic.AtomicReference;

public class StreamBasedSource<T> implements Source<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamBasedSource.class);

    private final Flux<T> stream;
    private final AtomicReference<T> latestValue;

    StreamBasedSource(Flux<T> backingstream, Scheduler scheduler) {
        this(backingstream, scheduler, null);
    }

    StreamBasedSource(Flux<T> backingstream, Scheduler scheduler, T defaultValue) {
        this.latestValue = new AtomicReference<>(defaultValue);
        this.stream = backingstream.publishOn(scheduler)
                .onBackpressureDrop(v -> LOGGER.warn("Sink had to drop value due to backpressure {}", v))
                .doOnNext(latestValue::set);
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

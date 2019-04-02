package cern.lhc.commons.web.property;

import reactor.core.publisher.Flux;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public class OverridableSource<T> implements Source<T> {

    private final Sink<T> result;

    private OverridableSource(Source<T> source, Source<T> override, Property<Boolean> overrideEnabler) {
        result = Sinks.createSink();

        AtomicReference<T> lastOverrideValue = new AtomicReference<>();
        AtomicReference<T> lastSourceValue = new AtomicReference<>();

        Flux<T> overrideStream = override.asStream();
        Flux<T> sourceStream = source.asStream();
        Flux<Boolean> overrideSwitchStream = overrideEnabler.asStream();

        Predicate<Object> isOverrideEnabled = any -> overrideEnabler.get();
        Predicate<Object> isOverrideDisabled = any -> !isOverrideEnabled.test(any);

        overrideStream.doOnNext(lastOverrideValue::set).filter(isOverrideEnabled).subscribe(result::push);
        sourceStream.doOnNext(lastSourceValue::set).filter(isOverrideDisabled).subscribe(result::push);

        overrideSwitchStream.subscribe(doOverride -> {
            T value = doOverride ? lastOverrideValue.get() : lastSourceValue.get();
            if (value != null) {
                result.push(value);
            }
        });
    }

    @Override
    public T getLatest() {
        return result.getLatest();
    }

    @Override
    public Flux<T> asStream() {
        return result.asStream();
    }

    public static <T> OverridableSourceBuilder<T> override(Source<T> source) {
        return new OverridableSourceBuilder<>(source);
    }

    public static class OverridableSourceBuilder<T> {
        private final Source<T> source;
        private Source<T> override;
        private Property<Boolean> overrideEnabler;

        private OverridableSourceBuilder(Source<T> source) {
            this.source = source;
        }

        public OverridableSourceBuilder<T> with(Source<T> override) {
            this.override = override;
            return this;
        }

        public OverridableSource<T> dependingOn(Property<Boolean> overrideEnabler) {
            Objects.requireNonNull(source);
            Objects.requireNonNull(override);
            Objects.requireNonNull(overrideEnabler);
            return new OverridableSource<>(source, override, overrideEnabler);
        }
    }
}

/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.lhc.commons.web.property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Functions.identity;

public class Properties {

    private static final Logger LOGGER = LoggerFactory.getLogger(Properties.class);

    private Properties() {
        throw new UnsupportedOperationException("Static Only!");
    }

    public static <T> Property<T> objectProperty(T initialValue) {
        return new SimpleProperty<>(initialValue);
    }

    public static <T> Property<T> objectProperty() {
        return objectProperty(null);
    }

    public static Property<Double> doubleProperty(double initialValue) {
        return new SimpleProperty<>(initialValue);
    }

    public static Property<Double> doubleProperty() {
        return doubleProperty(Double.NaN);
    }

    public static Property<Integer> intProperty(int initialValue) {
        return new SimpleProperty<>(initialValue);
    }

    public static Property<Integer> intProperty() {
        return intProperty(0);
    }

    public static Property<Boolean> booleanProperty(boolean initialValue) {
        return new SimpleProperty<>(initialValue);
    }

    public static Property<Boolean> booleanProperty() {
        return booleanProperty(false);
    }

    public static Property<String> stringProperty(String initialValue) {
        return new SimpleProperty<>(initialValue);
    }

    public static Property<String> stringProperty() {
        return stringProperty("");
    }

    public static <T> Property<T> streamBackedProperty(Flux<T> stream,
                                                       Consumer<T> consumer) {
        return new WrapperProperty<>(stream, consumer);
    }

    public static <S, D> Property<S> facadeProperty(Property<D> destination,
                                                    Function<S, D> mapper, Function<D, S> reverseMapper) {
        return new Property<S>() {

            @Override
            public S get() {
                return reverseMapper.apply(destination.get());
            }

            @Override
            public void set(S value) {
                destination.set(mapper.apply(value));
            }

            @Override
            public Source<S> getSource() {
                return Sources.sourceFrom(destination.asStream().map(reverseMapper));
            }

        };
    }

    public static <D> Property<String> facadeStringProperty(Property<D> destination, Function<String, D> mapper,
                                                            Function<D, String> reverseMapper) {
        return facadeProperty(destination, mapper, reverseMapper);
    }

    public static <D> Property<Double> facadeDoubleProperty(Property<D> destination, Function<Double, D> mapper,
                                                            Function<D, Double> reverseMapper) {
        return facadeProperty(destination, mapper, reverseMapper);
    }

    public static <D> Property<Integer> facadeIntProperty(Property<D> destination, Function<Integer, D> mapper,
                                                          Function<D, Integer> reverseMapper) {
        return facadeProperty(destination, mapper, reverseMapper);
    }

    public static <D> Property<Boolean> facadeBooleanProperty(Property<D> destination, Function<Boolean, D> mapper,
                                                              Function<D, Boolean> reverseMapper) {
        return facadeProperty(destination, mapper, reverseMapper);
    }

    public static <S, D> void bind(Property<S> source, Property<D> destination, Function<S, D> mapper) {
        source.asStream().subscribe(e -> {
            try {
                destination.set(mapper.apply(e));
            } catch (Exception ex) {
                LOGGER.warn("Error in bind, discarding update '{}'", e, ex);
            }
        });
    }

    public static <T> void bind(Property<T> source, Property<T> destination) {
        bind(source, destination, identity());
    }

    public static <S, D> void bindBidirectional(Property<S> source, Property<D> destination, Function<S, D> mapper,
                                                Function<D, S> reverseMapper) {
        bind(source, destination, mapper);
        bind(destination, source, reverseMapper);
    }

    public static <T> void bindBidirectional(Property<T> source, Property<T> destination) {
        bindBidirectional(source, destination, identity(), identity());
    }
}

package cern.lhc.commons.web.property;

import reactor.core.publisher.Flux;

import java.util.Objects;
import java.util.function.Function;

/**
 * Represents a mapping between a websocket path and a stream of data
 */
public class StreamWebsocketMapping<T> {

    private final Flux<T> stream;
    private final String path;
    private final Function<T, String> serialization;

    private StreamWebsocketMapping(Flux<T> stream, String path, Function<T, String> serialization) {
        this.stream = stream;
        this.path = path;
        this.serialization = serialization;
    }

    public static <T> StreamWebsocketMappingBuilder<T> websocketMappingFrom(Source<T> stream) {
        return new StreamWebsocketMappingBuilder<>(stream.asStream());
    }

    public static <T> StreamWebsocketMappingBuilder<T> websocketMappingFrom(Flux<T> stream) {
        return new StreamWebsocketMappingBuilder<>(stream);
    }

    public Flux<T> stream() {
        return stream;
    }

    public String path() {
        return path;
    }

    public Function<T, String> getSerialization() {
        return serialization;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        StreamWebsocketMapping that = (StreamWebsocketMapping) o;
        return Objects.equals(stream, that.stream) && Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stream, path);
    }

    @Override
    public String toString() {
        return "StreamWebsocketMapping{" + "path='" + path + '\'' + '}';
    }

    public static class StreamWebsocketMappingBuilder<T> {

        private final Flux<T> stream;
        private String path;

        private StreamWebsocketMappingBuilder(Flux<T> stream) {
            this.stream = Objects.requireNonNull(stream);
        }

        public StreamWebsocketMappingBuilder<T> withPath(String path) {
            if (path == null || path.isEmpty()) {
                throw new IllegalArgumentException("Websocket path cannot be empty");
            }
            this.path = path;
            return this;
        }

        public StreamWebsocketMapping<T> withDefaultSerialization() {
            Objects.requireNonNull(path);
            return new StreamWebsocketMapping<>(stream, path, JsonConversions::defaultSerialization);
        }

        public StreamWebsocketMapping<T> withCustomSerialization(Function<T, String> serialization) {
            Objects.requireNonNull(path);
            Objects.requireNonNull(serialization);
            return new StreamWebsocketMapping<>(stream, path, serialization);
        }
    }
}

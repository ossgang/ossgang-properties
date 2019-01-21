package cern.lhc.commons.web.property;

import reactor.core.publisher.Flux;

import java.util.Objects;

/**
 * Represents a mapping between a websocket path and a stream of data
 */
public class StreamWebsocketMapping {

    private final Flux<?> stream;
    private final String path;

    public StreamWebsocketMapping(Flux<?> stream, String path) {
        this.stream = stream;
        this.path = path;
    }

    public Flux<?> stream() {
        return stream;
    }

    public String path() {
        return path;
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
}

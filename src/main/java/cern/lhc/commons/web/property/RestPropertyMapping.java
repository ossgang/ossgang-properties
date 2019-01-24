package cern.lhc.commons.web.property;


import java.lang.reflect.Type;
import java.util.Objects;
import java.util.function.Function;

import static cern.lhc.commons.web.property.JsonConversions.defaultDeserialization;

/**
 * Represents a mapping between a REST endpoint and a property
 */
public class RestPropertyMapping<T> {

    private final Property<T> property;
    private final String path;
    private final Function<T, String> serialization;
    private final Function<String, T> deserialization;

    public RestPropertyMapping(Property<T> property, String path, Function<T, String> serialization, Function<String, T> deserialization) {
        this.property = property;
        this.path = path;
        this.serialization = serialization;
        this.deserialization = deserialization;
    }

    public static <T> RestPropertyMappingBuilder<T> mappingFor(Property<T> property) {
        return new RestPropertyMappingBuilder<>(property);
    }

    public static class RestPropertyMappingBuilder<T> {
        private final Property<T> property;
        private String path;

        private RestPropertyMappingBuilder(Property<T> property) {
            this.property = property;
        }

        public RestPropertyMappingBuilder<T> withPath(String path) {
            if (path == null || path.isEmpty()) {
                throw new IllegalArgumentException("Path cannot be empty");
            }
            this.path = path;
            return this;
        }

        public RestPropertyMapping<T> withDefaultSerializationAs(Class<T> clazz) {
            Objects.requireNonNull(path);
            Objects.requireNonNull(clazz);
            Function<String, T> deserialization = serialized -> defaultDeserialization(serialized, clazz);
            return new RestPropertyMapping<>(property, path, JsonConversions::defaultSerialization, deserialization);
        }

        public RestPropertyMapping<T> withDefaultSerializationAs(Type type) {
            Objects.requireNonNull(path);
            Objects.requireNonNull(type);
            Function<String, T> deserialization = serialized -> defaultDeserialization(serialized, type);
            return new RestPropertyMapping<>(property, path, JsonConversions::defaultSerialization, deserialization);
        }

        public RestPropertyMapping<T> withCustomSerialization(Function<T, String> serialization, Function<String, T> deserialization) {
            Objects.requireNonNull(path);
            Objects.requireNonNull(serialization);
            Objects.requireNonNull(deserialization);
            return new RestPropertyMapping<>(property, path, serialization, deserialization);
        }

    }

    public Property<?> property() {
        return property;
    }

    public String path() {
        return path;
    }

    public String serializeAndGet() {
        return serialization.apply(property.get());
    }

    public void deserializeAndSet(String serializedObject) {
        T deserialized = deserialization.apply(serializedObject);
        property.set(deserialized);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RestPropertyMapping<?> that = (RestPropertyMapping<?>) o;
        return Objects.equals(property, that.property) &&
                Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(property, path);
    }

    @Override
    public String toString() {
        return "RestPropertyMapping{" + "path='" + path + '\'' + '}';
    }
}

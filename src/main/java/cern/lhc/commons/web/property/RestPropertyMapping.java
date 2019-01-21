package cern.lhc.commons.web.property;

import java.util.Objects;

/**
 * Represents a mapping between a REST endpoint and a property
 */
public class RestPropertyMapping {

    private final Property<?> property;
    private final String path;

    public RestPropertyMapping(Property<?> property, String path) {
        this.property = property;
        this.path = path;
    }

    public Property<?> property() {
        return property;
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
        RestPropertyMapping that = (RestPropertyMapping) o;
        return Objects.equals(property, that.property) && Objects.equals(path, that.path);
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

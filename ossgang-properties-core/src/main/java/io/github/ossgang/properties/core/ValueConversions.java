/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package io.github.ossgang.properties.core;

import java.lang.reflect.Type;

import static io.github.ossgang.properties.core.JsonConversions.gson;


public class ValueConversions {

    private ValueConversions() {
        throw new UnsupportedOperationException("static only");
    }

    @SuppressWarnings({ "unchecked" })
    public static <T> T stringToValue(Type type, String value) {
        if (type == Double.class || type == Number.class) {
            return (T) (new Double(value));
        } else if (type == Integer.class) {
            return (T) (new Integer(value));
        } else if (type == Boolean.class) {
            return (T) (new Boolean(value));
        } else if (type == String.class) {
            return (T) value;
        } else {
            return gson().fromJson(value, type);
        }
    }

    public static String valueToString(Object value) {
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        } else if (value instanceof String) {
            return (String) value;
        } else {
            return gson().toJson(value);
        }
    }
}

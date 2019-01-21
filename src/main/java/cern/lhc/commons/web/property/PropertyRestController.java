/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.lhc.commons.web.property;

import cern.lhc.chroma.server.rbac.RbacProtected;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cern.lhc.commons.web.property.JsonConversions.gson;
import static java.lang.Double.parseDouble;

@RestController
@RequestMapping(PropertyRestController.PROPERTY_ENDPOINT)
public class PropertyRestController {

    public static final String PROPERTY_ENDPOINT = "properties";

    @Autowired
    private List<RestPropertyMapping> restProperties;

    @GetMapping("{endpoint}")
    public String get(@PathVariable("endpoint") String endpoint) {
        Property<?> property = restProperties.stream()
                .filter(e -> e.path().equals(endpoint))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Property '" + endpoint + "' is unknown.")).property();
        return valueToString(property.get());
    }

    @RbacProtected
    @PostMapping("{endpoint}")
    public void post(@PathVariable("endpoint") String endpoint, @RequestParam("value") String setValue) {
        Property<?> property = restProperties.stream().filter(e -> e.path().equals(endpoint)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Property '" + endpoint + "' is unknown.")).property();
        setPropertyFromString(property, setValue);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void setPropertyFromString(Property property, String setValue) {
        if (property.valueClass() == Double.class) {
            property.set(parseDouble(setValue));
        } else if (property.valueClass() == String.class) {
            property.set(setValue);
        } else {
            property.set(gson().fromJson(setValue, property.valueClass()));
        }
    }

    private static String valueToString(Object value) {
        if (value instanceof Double) {
            return value.toString();
        } else if (value instanceof String) {
            return (String) value;
        } else {
            return gson().toJson(value);
        }
    }

}

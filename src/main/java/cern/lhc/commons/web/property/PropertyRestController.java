/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.lhc.commons.web.property;

import cern.lhc.commons.web.rbac.RbacProtected;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(PropertyRestController.PROPERTY_ENDPOINT)
public class PropertyRestController {

    public static final String PROPERTY_ENDPOINT = "properties";

    @Autowired
    private List<RestPropertyMapping> restProperties;

    @GetMapping("{endpoint}")
    public String get(@PathVariable("endpoint") String endpoint) {
        RestPropertyMapping property = restProperties.stream()
                .filter(e -> e.path().equals(endpoint))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Property '" + endpoint + "' is unknown."));
        return property.serializeAndGet();
    }

    @RbacProtected
    @PostMapping("{endpoint}")
    public void post(@PathVariable("endpoint") String endpoint, @RequestParam("value") String setValue) {
        RestPropertyMapping property = restProperties.stream()
                .filter(e -> e.path().equals(endpoint))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Property '" + endpoint + "' is unknown."));

        property.deserializeAndSet(setValue);
    }

}

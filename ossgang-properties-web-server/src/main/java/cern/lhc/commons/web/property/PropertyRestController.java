/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.lhc.commons.web.property;

import cern.lhc.commons.web.rbac.RbacProtected;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(PropertyRestController.PROPERTY_ENDPOINT)
public class PropertyRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyRestController.class);
    public static final String PROPERTY_ENDPOINT = "properties";

    @Autowired(required = false)
    private List<RestPropertyMapping> restProperties;

    @PostConstruct
    private void init() {
        properties().forEach(mapping -> LOGGER.info("Mapped rest property {}", mapping.path()));
    }

    @GetMapping("{endpoint}")
    public String get(@PathVariable("endpoint") String endpoint) {
        RestPropertyMapping property = properties().stream()
                .filter(e -> e.path().equals(endpoint))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Property '" + endpoint + "' is unknown."));
        return property.serializeAndGet();
    }

    @RbacProtected
    @PostMapping("{endpoint}")
    public void post(@PathVariable("endpoint") String endpoint, @RequestParam("value") String setValue) {
        RestPropertyMapping property = properties().stream()
                .filter(e -> e.path().equals(endpoint))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Property '" + endpoint + "' is unknown."));

        property.deserializeAndSet(setValue);
    }

    List<RestPropertyMapping> properties() {
        return Optional.ofNullable(restProperties).orElse(Collections.emptyList());
    }
}

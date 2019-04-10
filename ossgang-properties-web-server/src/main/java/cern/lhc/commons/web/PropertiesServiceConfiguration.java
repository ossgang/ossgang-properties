package cern.lhc.commons.web;

import cern.lhc.commons.web.property.PropertyRestController;
import cern.lhc.commons.web.property.PropertyWebsocketConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({PropertyWebsocketConfiguration.class, PropertyRestController.class})
public class PropertiesServiceConfiguration {
}

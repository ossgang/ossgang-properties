package cern.lhc.commons.web.property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;

import java.util.List;
import java.util.stream.Stream;

import static cern.lhc.commons.web.property.PropertyRestController.PROPERTY_ENDPOINT;
import static cern.lhc.commons.web.property.StreamWebSocketHandler.websocketFromStream;
import static com.google.common.collect.Streams.concat;
import static java.util.stream.Collectors.toList;

@Configuration
public class PropertyWebsocketConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyWebsocketConfiguration.class);

    @Bean
    public List<StreamWebsocketMapping> propertyWebsockets(List<RestPropertyMapping> restProperties) {
        return restProperties.stream().map(r -> new StreamWebsocketMapping(r.property().asStream(),
                PROPERTY_ENDPOINT + "/ws/" + r.path())).collect(toList());
    }

    @Bean
    public WebSocketConfigurer registerWebSocketHandlers(List<StreamWebsocketMapping> handlers,
            List<List<StreamWebsocketMapping>> bulkHandlers) {
        Stream<StreamWebsocketMapping> allHandlers = concat(handlers.stream(),
                bulkHandlers.stream().flatMap(List::stream));
        return registry -> allHandlers.forEach(h -> {
            LOGGER.info("Mapping websocket handler for {}", h.path());
            registry.addHandler(websocketFromStream(h.stream()), h.path());
        });
    }

}

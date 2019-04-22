package cern.lhc.commons.web.property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;

import java.util.List;
import java.util.stream.Stream;

import static cern.lhc.commons.web.property.PropertyRestController.PROPERTY_ENDPOINT;
import static cern.lhc.commons.web.property.StreamWebSocketHandler.websocketFrom;
import static cern.lhc.commons.web.property.StreamWebsocketMapping.websocketMappingFrom;
import static com.google.common.collect.Streams.concat;
import static java.util.stream.Collectors.toList;

@Configuration
@EnableWebSocket
public class PropertyWebsocketConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyWebsocketConfiguration.class);

    @Bean
    @SuppressWarnings("unchecked") /* safe because it is enforced in the RestPropertyMapping */
    public List<StreamWebsocketMapping> propertyWebsockets(List<RestPropertyMapping> restProperties) {
        return restProperties.stream()
                .map(r -> websocketMappingFrom(r.property().getSource())
                        .withPath(PROPERTY_ENDPOINT + "/ws/" + r.path())
                        .withCustomSerialization(r.getSerialization())
                )
                .collect(toList());
    }

    @Bean
    public WebSocketConfigurer registerWebSocketHandlers(List<StreamWebsocketMapping> wsMappers,
                                                         List<List<StreamWebsocketMapping>> bulkWsMappers) {


        Stream<StreamWebsocketMapping> allMappers = concat(wsMappers.stream(),
                bulkWsMappers.stream().flatMap(List::stream));

        return registry -> allMappers.forEach(m -> {
            LOGGER.info("Mapping websocket handler for {}", m.path());
            registry.addHandler(websocketFrom(m), m.path());
        });
    }

}

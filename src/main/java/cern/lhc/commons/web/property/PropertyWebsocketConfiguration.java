package cern.lhc.commons.web.property;

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
        return registry -> allHandlers.forEach(h -> registry.addHandler(websocketFromStream(h.stream()), h.path()));
    }

}

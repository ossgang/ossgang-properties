package cern.lhc.commons.web.property;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class StreamWebSocketHandler extends TextWebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamWebSocketHandler.class);
    private static final Gson GSON = JsonConversions.gson();
    private static final String SESSION_STREAM_SUBSCRIPTION = "SESSION_STREAM_SUBSCRIPTION";

    private final Flux<String> stream;

    private StreamWebSocketHandler(Flux<String> stream) {
        this.stream = stream;
    }

    public static StreamWebSocketHandler websocketFromStream(Flux<?> stream) {
        return new StreamWebSocketHandler(stream.map(GSON::toJson));
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        LOGGER.info("Websocket connection {} initialized", session.getId());
        Disposable subscription = stream //
                .publishOn(Schedulers.elastic()) //
                .subscribe(value -> sendMessage(session, value), e -> LOGGER.error("ERROR", e),
                        () -> LOGGER.info("COMPLETE !!"));

        session.getAttributes().put(SESSION_STREAM_SUBSCRIPTION, subscription);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        LOGGER.info("Websocket connection {} closed with status {}", session.getId(), status);
        ((Disposable) session.getAttributes().get(SESSION_STREAM_SUBSCRIPTION)).dispose();
    }

    private void sendMessage(WebSocketSession session, String chroma) {
        try {
            session.sendMessage(new TextMessage(chroma));
        } catch (Exception e) {
            LOGGER.warn("Websocket connection {}, cannot send message. Attempting to close...", session.getId(), e);
            closeConnection(session);
        }
    }

    private void closeConnection(WebSocketSession session) {
        try {
            session.close();
            LOGGER.info("Websocket connection {} closed", session.getId());
        } catch (Exception e1) {
            LOGGER.warn("Could not close websocket connection {}", session.getId(), e1);
        }
    }
}

package cern.lhc.commons.web.property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import reactor.core.Disposable;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class StreamWebSocketHandler extends TextWebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamWebSocketHandler.class);
    private static final String SESSION_STREAM_SUBSCRIPTION = "SESSION_STREAM_SUBSCRIPTION";
    private static final Scheduler DEFAULT_SCHEDULER = Schedulers.elastic();

    private final StreamWebsocketMapping<Object> mapping;

    private StreamWebSocketHandler(StreamWebsocketMapping<Object> mapping) {
        this.mapping = mapping;
    }

    public static StreamWebSocketHandler websocketFrom(StreamWebsocketMapping<Object> mapping) {
        return new StreamWebSocketHandler(mapping);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        LOGGER.info("Websocket connection {} initialized", session.getId());
        Disposable subscription = mapping //
                .stream() //
                .map(mapping.getSerialization()) //
                .replay(1).autoConnect() // Replay the latest value for late subscribers!
                .publishOn(DEFAULT_SCHEDULER) //
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

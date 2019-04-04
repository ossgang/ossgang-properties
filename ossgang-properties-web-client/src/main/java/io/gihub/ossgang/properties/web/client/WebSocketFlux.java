/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package io.gihub.ossgang.properties.web.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Thread.sleep;

/**
 * Provides utility methods to create fluxes from web sockets.
 *
 * @author kfuchsbe
 */
public class WebSocketFlux {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketFlux.class);
    private static final Executor RECONNECT_EXECUTOR = Executors.newCachedThreadPool();
    private static final StandardWebSocketClient WS_CLIENT = new StandardWebSocketClient();
    private static final int ONE_MEGABYTE = 1 << 20;

    private WebSocketFlux() {
        throw new UnsupportedOperationException("static only");
    }

    /**
     * Creates a flux of Strings from the given websocket uri. The current implementation cahces the last value for late
     * subscribers. It does never reconnect nor error. For the moment it just logs on errors, but the connection might
     * be lost.
     *
     * @param uri the uri to connect the stream to (Must start with the web socket protocol: 'ws://').
     * @return a flux delivering the websocket messages as string
     */
    public static final Flux<String> fluxFrom(String uri) {
        return new StringFluxWebSocketHandler(uri).stream();
    }

    /**
     * A handler to be added to a web socket client, which only accepts text messages and provides a flux of strings.
     *
     * @author kfuchsbe
     */
    private static class StringFluxWebSocketHandler extends TextWebSocketHandler {

        private static final int RECONNECT_RETRY_MAX = 10;
        private static final int RETRY_INTERVAL_SEC = 10;

        private final ReplayProcessor<String> sink = ReplayProcessor.cacheLast();
        private final Flux<String> stream = sink.publishOn(Schedulers.elastic());
        private final String uri;
        private final AtomicBoolean reconnecting = new AtomicBoolean(false);

        StringFluxWebSocketHandler(String uri) {
            this.uri = uri;
            scheduleReconnectionAttempt();
        }

        private synchronized void connect() {
            ListenableFuture<WebSocketSession> future = WS_CLIENT.doHandshake(this, uri);
            try {
                WebSocketSession session = future.get();
                LOGGER.info("Successfully connected to websocket {} with id {}", uri, session.getId());
                session.setBinaryMessageSizeLimit(ONE_MEGABYTE);
                session.setTextMessageSizeLimit(ONE_MEGABYTE);
            } catch (Exception e) {
                LOGGER.error("Cannot reconnect to {}: {}", uri, e.getMessage());
                throw new IllegalStateException("Cannot connect to " + uri, e);
            }
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            LOGGER.trace("Received text message from '{}': '{}'", uri, message.getPayload());
            sink.onNext(message.getPayload());
        }

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            LOGGER.trace("Connection established to {} with id {}", uri, session.getId());
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
            if (reconnecting.get()) {
                LOGGER.warn("Transport error from connection {} ({}) while still performing connections operations. " +
                        "Ignoring it", session.getId(), uri);
                return;
            }
            LOGGER.error("Transport error on connection {} ({}). Closing it", session.getId(), uri, exception);
            session.close();
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
            LOGGER.warn("Connection {} ({}) closed. Status {}. Trying to reconnect...", session.getId(), uri, status);
            scheduleReconnectionAttempt();
        }

        private void scheduleReconnectionAttempt() {
            if (!reconnecting.compareAndSet(false, true)) {
                LOGGER.warn("Attempt to connect to {} while already reconnecting.. doing nothing", uri);
                return;
            }

            LOGGER.info("Connecting to {}...", uri);
            RECONNECT_EXECUTOR.execute(() -> {
                boolean reconnected = false;
                for (int i = 0; i < RECONNECT_RETRY_MAX; i++) {
                    try {
                        connect();
                        reconnected = true;
                        break;
                    } catch (Exception e) {
                        LOGGER.warn("Attempt[{}]: connection to {} failed.. retrying in {} seconds", i, uri,
                                RETRY_INTERVAL_SEC);
                    }
                    try {
                        sleep(RETRY_INTERVAL_SEC * 1000);
                    } catch (InterruptedException e) {
                        LOGGER.error("Interrupted", e);
                    }
                }

                if (!reconnected) {
                    LOGGER.error("Could not reconnect to {} after {} attempts. Giving up", uri, RECONNECT_RETRY_MAX);
                    sink.onComplete();
                }

                reconnecting.set(false);
            });
        }

        Flux<String> stream() {
            return this.stream;
        }

    }

}

package cern.lhc.commons.web.property;

/**
 * Convenience class for mapping a sink to a path for a websockets connections
 */
public class SinkWebSocketMapping extends StreamWebsocketMapping {

    public SinkWebSocketMapping(Sink<?> sink, String path) {
        super(sink.asStream(), path);
    }
}

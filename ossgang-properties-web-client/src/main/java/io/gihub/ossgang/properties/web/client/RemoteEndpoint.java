package io.gihub.ossgang.properties.web.client;

public interface RemoteEndpoint {

    String relativeUrl();

    default String name() {
        return relativeUrl();
    }

}

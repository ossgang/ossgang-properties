package io.gihub.ossgang.properties.web.client.rbac;

public interface RbacTokenProvider {

    /**
     * Retrieve the current RBAC token. NOTE: the byte[] return type makes it easy to implement a demo mode
     */
    byte[] getRbacToken();

}

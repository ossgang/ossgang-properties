package io.gihub.ossgang.properties.web.client.rbac;

public class DemoRbacTokenProvider implements RbacTokenProvider {

    private static final byte[] DEMO_TOKEN = "demo-token".getBytes();

    @Override
    public byte[] getRbacToken() {
        return DEMO_TOKEN;
    }

}

package cern.lhc.commons.web.rbac;


import cern.rbac.common.RbaToken;
import cern.rbac.common.TokenFormatException;

import java.nio.ByteBuffer;
import java.util.Base64;

public final class RbacUtils {

    public static String encodeRbacToken(RbaToken token) {
        return Base64.getEncoder().encodeToString(token.getEncoded());
    }

    public static RbaToken decodeRbacToken(String token) {
        try {
            return RbaToken.parseNoValidate(ByteBuffer.wrap(Base64.getDecoder().decode(token)));
        } catch (TokenFormatException e) {
            throw new IllegalArgumentException("Invalid format for RBAC token", e);
        }
    }

    private RbacUtils() {
        throw new UnsupportedOperationException("Static only!");
    }
}

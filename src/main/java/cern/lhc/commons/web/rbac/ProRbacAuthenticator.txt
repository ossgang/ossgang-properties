package cern.lhc.commons.web.rbac;

import cern.rbac.client.authorization.AuthorizationClient;
import cern.rbac.common.RbaToken;
import cern.rbac.common.authorization.AuthorizationException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Real RBAC authenticator that validated that the provided tokens are allowed to do the specified operation on the
 * {@link RbacProtectionProperty}
 */
public class ProRbacAuthenticator implements RbacAuthenticator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProRbacAuthenticator.class);

    private final RbacProtectionProperty protectionProperty;
    private final LoadingCache<RbaToken, Boolean> accessCheckResults;

    public ProRbacAuthenticator(RbacProtectionProperty protectionProperty) {
        this.protectionProperty = protectionProperty;
        this.accessCheckResults =
                CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build(CacheLoader.from(this::checkAccess));
    }

    private Boolean checkAccess(RbaToken token) {
        AuthorizationClient checker = AuthorizationClient.create();
        try {
            LOGGER.info("Token {} not in cache, asking RBAC", token);
            return checker.isAuthorized(token, protectionProperty.deviceClass(), protectionProperty.deviceName(),
                    protectionProperty.propertyName(), protectionProperty.operation().ccdbName());
        } catch (AuthorizationException e) {
            throw new SecurityException("Error checking access via RBAC", e);
        }
    }

    @Override
    public boolean isTokenAuthorized(String token) {
        try {
            return accessCheckResults.get(RbacUtils.decodeRbacToken(token));
        } catch (ExecutionException e) {
            throw new IllegalStateException("Error checking valid RBAC token", e);
        }
    }

}

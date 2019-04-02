package cern.lhc.commons.web.rbac;

/**
 * Contains the logic to validate an RBAC token
 */
public interface RbacAuthenticator {

    /**
     * Returns whether the provided token is valid or not. What is considered valid depends on the implementation
     */
    boolean isTokenAuthorized(String token);

}

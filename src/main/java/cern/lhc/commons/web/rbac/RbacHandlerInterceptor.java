package cern.lhc.commons.web.rbac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

/**
 * Spring interceptor that validates the user RBAC token before allowing the request to go through
 *
 * @see RbacProtected
 */
public class RbacHandlerInterceptor extends HandlerInterceptorAdapter {

    private static final String RBAC_TOKEN_HEADER = "X-RBAC-TOKEN";
    private static final Logger LOGGER = LoggerFactory.getLogger(RbacHandlerInterceptor.class);

    private final RbacAuthenticator rbacAuthenticator;

    public RbacHandlerInterceptor(RbacAuthenticator rbacAuthenticator) {
        this.rbacAuthenticator = rbacAuthenticator;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        StringBuffer url = request.getRequestURL();

        if (!isRbacProtected(handler)) {
            LOGGER.trace("{} is NOT protected by RBAC, allowing request", url);
            return true;
        }

        Optional<String> rbacToken = extractRbacToken(request);

        if (!rbacToken.isPresent()) {
            LOGGER.error("{} is protected by RBAC but no suitable header found in: {}", url, headersOf(request));
            sendForbiddenStatusCode(request, response);
            return false;
        }

        if (!rbacAuthenticator.isTokenAuthorized(rbacToken.get())) {
            LOGGER.error("{} request RBAC token is NOT allowed to continue. Rejecting request", url);
            sendForbiddenStatusCode(request, response);
            return false;
        }

        LOGGER.info("{} request has been validated via RBAC token", url);
        return true;
    }

    private static boolean isRbacProtected(Object handler) {
        if(handler instanceof ResourceHttpRequestHandler) {
            return false;
        }

        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            return handlerMethod.getMethod().getAnnotation(RbacProtected.class) != null;
        }

        throw new UnsupportedOperationException("Cannot have handlers other than HandlerMethod and ResourceHttpRequestHandler in Spring. RBAC " + "protection not provided in such cases");
    }

    private static Optional<String> extractRbacToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(RBAC_TOKEN_HEADER));
    }

    private static String headersOf(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames()).toString();
    }

    private static void sendForbiddenStatusCode(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.sendError(HttpStatus.FORBIDDEN.value());
        } catch (IOException e) {
            LOGGER.error("Exception while rejecting request {}", request.getRequestURL(), e);
        }
    }

}

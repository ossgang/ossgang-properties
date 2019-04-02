package cern.lhc.commons.web.rbac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

/**
 * Demo implementation that will reject any token if the spring profile is pro or production
 */
public class DemoRbacAuthentication implements RbacAuthenticator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoRbacAuthentication.class);

    @Autowired
    private Environment environment;

    @Override
    public boolean isTokenAuthorized(String token) {
        LOGGER.info("Validating RBAC token in DEMO mode!");
        return environment.acceptsProfiles("!pro", "!production");
    }

}

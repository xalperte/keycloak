package org.keycloak.adapters.springsecurity.filter;

import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.adapters.HttpFacade;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.PreAuthActionsHandler;
import org.keycloak.adapters.UserSessionManagement;
import org.keycloak.adapters.springsecurity.AdapterDeploymentContextBean;
import org.keycloak.adapters.springsecurity.facade.SimpleHttpFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * Exposes a Keycloak adapter {@link PreAuthActionsHandler} as a Spring Security filter.
 *
 * @author <a href="mailto:srossillo@smartling.com">Scott Rossillo</a>
 * @version $Revision: 1 $
 */
public class KeycloakPreAuthActionsFilter extends GenericFilterBean implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(KeycloakPreAuthActionsFilter.class);

    private ApplicationContext applicationContext;
    private AdapterDeploymentContext deploymentContext;
    private UserSessionManagement userSessionManagement;

    public KeycloakPreAuthActionsFilter() {
        super();
    }

    public KeycloakPreAuthActionsFilter(UserSessionManagement userSessionManagement) {
        this.userSessionManagement = userSessionManagement;
    }

    @Override
    protected void initFilterBean() throws ServletException {
        AdapterDeploymentContextBean contextBean = applicationContext.getBean(AdapterDeploymentContextBean.class);
        deploymentContext = contextBean.getDeploymentContext();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpFacade facade = new SimpleHttpFacade((HttpServletRequest)request, (HttpServletResponse)response);
        PreAuthActionsHandler handler = new PreAuthActionsHandler(userSessionManagement, deploymentContext, facade);
        if (handler.handleRequest()) {
            log.info("Pre-auth filter handled request: {}", ((HttpServletRequest) request).getRequestURI());
        } else {
            chain.doFilter(request, response);
        }
    }

    public void setUserSessionManagement(UserSessionManagement userSessionManagement) {
        this.userSessionManagement = userSessionManagement;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

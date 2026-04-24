package com.smartcampusapi.logging;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;

// this filter logs every request and response that goes through the api
// implements both request and response filters so we can see the full picture
// way better than putting log statements in every single controller method
@Provider
public class RequestResponseLogger implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(RequestResponseLogger.class.getName());

    @Override
    public void filter(ContainerRequestContext requestCtx) {
        // log the incoming request - method and uri
        LOG.info(">> incoming: " + requestCtx.getMethod() + " " + requestCtx.getUriInfo().getRequestUri());
    }

    @Override
    public void filter(ContainerRequestContext requestCtx, ContainerResponseContext responseCtx) {
        // log the outgoing response status
        LOG.info("<< outgoing: " + responseCtx.getStatus() + " for "
                + requestCtx.getMethod() + " " + requestCtx.getUriInfo().getRequestUri());
    }
}


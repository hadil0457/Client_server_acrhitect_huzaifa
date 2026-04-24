package com.smartcampusapi.errors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

// catch-all mapper that handles any exception we didnt specifically handle elsewhere
// this is basically our safety net so the api never leaks java stack traces to the client
// that would be a security risk because attackers could see internal class names, paths etc
@Provider
public class CatchAllExMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(CatchAllExMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {
        // if its a jax-rs exception like NotFoundException, let it keep its own status code
        // we dont want to turn a 404 into a 500 by accident
        if (ex instanceof WebApplicationException) {
            WebApplicationException webEx = (WebApplicationException) ex;
            int code = webEx.getResponse().getStatus();
            ErrorBody body = new ErrorBody(code, Response.Status.fromStatusCode(code).getReasonPhrase(), ex.getMessage());
            return Response.status(code)
                    .entity(body)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // log the actual error so we can debug it from server logs
        LOG.log(Level.SEVERE, "unhandled exception caught by catch-all mapper", ex);

        // send back a generic 500 without any internal details
        ErrorBody body = new ErrorBody(
            500,
            "Internal Server Error",
            "something went wrong on the server, please try again later"
        );
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}


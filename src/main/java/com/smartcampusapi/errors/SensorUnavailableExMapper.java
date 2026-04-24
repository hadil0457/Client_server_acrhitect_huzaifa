package com.smartcampusapi.errors;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

// maps SensorUnavailableException to 403 Forbidden
// this happens when you try to post a reading to a sensor in MAINTENANCE mode
@Provider
public class SensorUnavailableExMapper implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException ex) {
        ErrorBody body = new ErrorBody(403, "Forbidden", ex.getMessage());
        return Response.status(Response.Status.FORBIDDEN)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}


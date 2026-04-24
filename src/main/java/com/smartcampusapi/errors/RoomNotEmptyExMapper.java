package com.smartcampusapi.errors;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

// maps RoomNotEmptyException to a 409 Conflict response
// this fires when someone tries to delete a room that still has sensors
@Provider
public class RoomNotEmptyExMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException ex) {
        ErrorBody body = new ErrorBody(409, "Conflict", ex.getMessage());
        return Response.status(Response.Status.CONFLICT)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}


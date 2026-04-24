package com.smartcampusapi.errors;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

// maps LinkedResourceNotFoundException to 422
// happens when a sensor is posted with a roomId that doesnt exist
@Provider
public class LinkedResourceNotFoundExMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        // using 422 because the json itself is valid, but the data inside it references
        // something that doesnt exist - so its "unprocessable"
        ErrorBody body = new ErrorBody(422, "Unprocessable Entity", ex.getMessage());
        return Response.status(422)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}


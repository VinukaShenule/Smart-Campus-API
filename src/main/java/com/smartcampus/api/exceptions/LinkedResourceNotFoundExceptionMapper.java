package com.smartcampus.api.exceptions;

import com.smartcampus.api.models.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/** 422 Unprocessable Entity: roomId in sensor payload does not exist */
@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {
    @Override
    public Response toResponse(LinkedResourceNotFoundException e) {
        return Response.status(422)
                .entity(new ErrorResponse(422, "Unprocessable Entity", e.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}

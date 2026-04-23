package com.smartcampus.api.exceptions;

import com.smartcampus.api.models.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/** 404 Not Found: Room or Sensor does not exist */
@Provider
public class ResourceNotFoundExceptionMapper implements ExceptionMapper<ResourceNotFoundException> {
    @Override
    public Response toResponse(ResourceNotFoundException e) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse(404, "Not Found", e.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}

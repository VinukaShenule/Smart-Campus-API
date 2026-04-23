package com.smartcampus.api.exceptions;

import com.smartcampus.api.models.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/** 409 Conflict: Room has sensors still assigned */
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {
    @Override
    public Response toResponse(RoomNotEmptyException e) {
        return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse(409, "Conflict", e.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}

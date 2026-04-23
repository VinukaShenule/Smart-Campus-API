package com.smartcampus.api.exceptions;

import com.smartcampus.api.models.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 500 Global catch-all.
 * Logs full stack trace server-side but never exposes it to the client.
 * Exposing stack traces leaks: class names, library versions, file paths,
 * and internal logic - all valuable to an attacker.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable e) {
        LOGGER.log(Level.SEVERE, "Unhandled exception: " + e.getMessage(), e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse(500, "Internal Server Error",
                        "An unexpected error occurred. Please contact the administrator."))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}

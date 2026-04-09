package com.smartcampus.mapper;

import com.smartcampus.dto.ErrorResponse;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global "safety net" exception mapper.
 * Catches any unhandled exception and returns a generic 500 JSON response.
 * Preserves the correct status code for JAX-RS built-in exceptions
 * (e.g. NotFoundException -> 404, NotAllowedException -> 405).
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // If JAX-RS itself threw the exception (404, 405, 415, etc.),
        // preserve its status code instead of returning 500.
        if (exception instanceof WebApplicationException) {
            WebApplicationException webEx = (WebApplicationException) exception;
            int status = webEx.getResponse().getStatus();
            String reasonPhrase = webEx.getResponse().getStatusInfo().getReasonPhrase();

            LOGGER.log(Level.WARNING, "JAX-RS exception: {0} {1} - {2}",
                    new Object[]{status, reasonPhrase, exception.getMessage()});

            ErrorResponse error = new ErrorResponse(status, reasonPhrase, exception.getMessage());
            return Response.status(status)
                    .entity(error)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // For everything else (NullPointerException, etc.), return a safe 500.
        LOGGER.log(Level.SEVERE, "Unhandled exception caught: " + exception.getMessage(), exception);

        ErrorResponse error = new ErrorResponse(
                500,
                "Internal Server Error",
                "An unexpected error occurred. Please contact the system administrator."
        );
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}

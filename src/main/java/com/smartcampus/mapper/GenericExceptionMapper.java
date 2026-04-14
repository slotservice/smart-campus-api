package com.smartcampus.mapper;

import com.smartcampus.dto.ErrorResponse;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
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

        LOGGER.log(Level.SEVERE, "Unhandled exception caught: " + exception.getMessage(), exception);

        ErrorResponse error = new ErrorResponse(500, "Internal Server Error",
                "An unexpected error occurred. Please contact the system administrator.");
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}

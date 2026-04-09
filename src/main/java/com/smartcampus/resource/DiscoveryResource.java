package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response getApiInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("apiName", "Smart Campus Sensor & Room Management API");
        info.put("version", "1.0");
        info.put("description", "RESTful API for managing campus rooms, sensors, and their readings.");
        info.put("adminContact", "admin@smartcampus.ac.uk");

        Map<String, String> links = new LinkedHashMap<>();
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        info.put("links", links);

        return Response.ok(info).build();
    }
}

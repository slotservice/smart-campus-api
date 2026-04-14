package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static final String BASE_URI = "http://localhost:8080/api/v1/";

    public static HttpServer startServer() {
        final ResourceConfig config = new ResourceConfig()
                .packages("com.smartcampus.resource",
                          "com.smartcampus.mapper",
                          "com.smartcampus.filter");

        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);
    }

    public static void main(String[] args) {
        final HttpServer server = startServer();
        Logger.getLogger(Main.class.getName()).log(Level.INFO,
                "Smart Campus API started at {0}", BASE_URI);
        System.out.println("===========================================");
        System.out.println("Smart Campus API is running!");
        System.out.println("Base URL: " + BASE_URI);
        System.out.println("Test it:  " + BASE_URI);
        System.out.println("Press ENTER to stop the server...");
        System.out.println("===========================================");
        try {
            System.in.read();
        } catch (Exception e) {
            // ignore
        }
        server.shutdownNow();
    }
}

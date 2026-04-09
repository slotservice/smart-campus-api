package com.smartcampus.config;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/api/v1")
public class ApplicationConfig extends Application {
    // JAX-RS will automatically scan for annotated resource and provider classes
}

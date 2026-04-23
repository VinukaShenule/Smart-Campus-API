package com.smartcampus.api;

import com.smartcampus.api.exceptions.GlobalExceptionMapper;
import com.smartcampus.api.exceptions.LinkedResourceNotFoundExceptionMapper;
import com.smartcampus.api.exceptions.ResourceNotFoundExceptionMapper;
import com.smartcampus.api.exceptions.RoomNotEmptyExceptionMapper;
import com.smartcampus.api.exceptions.SensorUnavailableExceptionMapper;
import com.smartcampus.api.filters.LoggingFilter;
import com.smartcampus.api.resources.DiscoveryResource;
import com.smartcampus.api.resources.RoomResource;
import com.smartcampus.api.resources.SensorResource;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * JAX-RS Application entry point.
 *
 * Extends javax.ws.rs.core.Application (standard JAX-RS 2.1 API, compatible with Jersey 2.x
 * and Tomcat 9). All resource classes, filters and exception mappers are explicitly registered
 * via getClasses() for reliable deployment.
 *
 * NOTE on namespace:
 *   - javax.ws.rs  ->  JAX-RS 2.1  ->  Jersey 2.x  ->  Tomcat 9  (this project)
 *   - jakarta.ws.rs -> JAX-RS 3.0  ->  Jersey 3.x  ->  Tomcat 10+
 *
 * Lifecycle: JAX-RS creates a NEW resource instance per request (not singleton).
 * Shared in-memory data lives in DataStore (ConcurrentHashMap singleton) to prevent
 * race conditions across concurrent requests.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        // Resource endpoints
        classes.add(DiscoveryResource.class);
        classes.add(RoomResource.class);
        classes.add(SensorResource.class);

        // Cross-cutting filter (request/response logging)
        classes.add(LoggingFilter.class);

        // Exception mappers
        classes.add(RoomNotEmptyExceptionMapper.class);
        classes.add(LinkedResourceNotFoundExceptionMapper.class);
        classes.add(SensorUnavailableExceptionMapper.class);
        classes.add(ResourceNotFoundExceptionMapper.class);
        classes.add(GlobalExceptionMapper.class);

        return classes;
    }
}

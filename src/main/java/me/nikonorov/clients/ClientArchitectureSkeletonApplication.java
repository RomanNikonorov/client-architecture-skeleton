package me.nikonorov.clients;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Spring Boot entry point for the client integration service.
 *
 * <p>The application scans configuration properties and component beans under
 * {@code me.nikonorov.clients}. New REST, gRPC, application, domain, and
 * infrastructure classes should stay under this root package so Spring and
 * Spring Modulith can discover them consistently.</p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ClientArchitectureSkeletonApplication {

    /**
     * Starts the Spring Boot application.
     *
     * @param args command line arguments passed by the runtime
     */
    public static void main(String[] args) {
        SpringApplication.run(ClientArchitectureSkeletonApplication.class, args);
    }
}

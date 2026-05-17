/**
 * Root Spring Modulith application module for client integration capabilities.
 *
 * <p>All packages under this root belong to the same module in the current
 * layered architecture. Dependencies between layers are enforced separately by
 * architecture tests.</p>
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Clients",
        allowedDependencies = {}
)
package me.nikonorov.clients;

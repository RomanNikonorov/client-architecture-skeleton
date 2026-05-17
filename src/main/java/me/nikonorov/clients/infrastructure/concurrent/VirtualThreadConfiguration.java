package me.nikonorov.clients.infrastructure.concurrent;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Infrastructure configuration for virtual-thread execution.
 *
 * <p>The executor bean is deliberately declared in infrastructure so
 * application use cases depend only on {@link me.nikonorov.clients.application.FanOutExecutor}
 * and cannot accidentally own thread-management policy.</p>
 */
@Configuration
class VirtualThreadConfiguration {

    /**
     * Creates the shared virtual-thread-per-task executor.
     *
     * <p>The executor is closed by Spring during application shutdown.</p>
     *
     * @return executor used by infrastructure fan-out components
     */
    @Bean(destroyMethod = "close")
    ExecutorService virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}

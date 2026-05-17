package me.nikonorov.clients.application;

import java.util.function.Supplier;

/**
 * Central application-level API for bounded parallel fan-out.
 *
 * <p>Use cases must use this interface instead of directly creating
 * {@link java.util.concurrent.ExecutorService}, {@link java.util.concurrent.Semaphore},
 * {@link java.lang.Thread}, or ad hoc {@link java.util.concurrent.CompletableFuture}
 * orchestration. This keeps virtual-thread policy, per-request limits, failure
 * unwrapping, and interrupt handling in one infrastructure implementation.</p>
 */
public interface FanOutExecutor {

    /**
     * Opens an isolated fan-out scope for one application operation.
     *
     * <p>The limit applies only within the returned scope. Opening multiple
     * scopes creates independent per-request limits; it does not create or
     * configure a global service-wide bulkhead.</p>
     *
     * @param maxParallelTasks maximum number of tasks that may execute at once
     *                         inside this scope; implementations must normalize
     *                         values below {@code 1}
     * @return a scope used to submit related parallel tasks
     */
    FanOutScope openScope(int maxParallelTasks);

    /**
     * Request-local group of bounded parallel tasks.
     *
     * <p>A scope is intentionally lightweight and is expected to be created by
     * a use case for each incoming command that needs fan-out.</p>
     */
    interface FanOutScope {

        /**
         * Submits a task to the scope.
         *
         * <p>The supplier may perform blocking I/O. The implementation decides
         * when the task can start based on scope capacity.</p>
         *
         * @param supplier task body to execute
         * @param <T> result type
         * @return handle used to join the task result
         */
        <T> FanOutTask<T> submit(Supplier<T> supplier);
    }

    /**
     * Handle for a submitted fan-out task.
     *
     * @param <T> result type produced by the task
     */
    interface FanOutTask<T> {

        /**
         * Waits for the task to finish and returns its result.
         *
         * <p>Implementations should surface application failures as directly as
         * possible instead of forcing use cases to handle transport-specific or
         * concurrency-specific wrapper exceptions.</p>
         *
         * @return completed task result
         */
        T join();
    }
}

package me.nikonorov.clients.infrastructure.concurrent;

import me.nikonorov.clients.application.FanOutExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

/**
 * Virtual-thread implementation of the application fan-out contract.
 *
 * <p>Each submitted task runs on the injected virtual-thread executor. A
 * request-local {@link Semaphore} limits how many tasks from one scope may run
 * simultaneously.</p>
 */
@Component
class VirtualThreadFanOutExecutor implements FanOutExecutor {

    private final ExecutorService virtualThreadExecutor;

    /**
     * Creates the executor adapter.
     *
     * @param virtualThreadExecutor shared virtual-thread-per-task executor bean
     */
    VirtualThreadFanOutExecutor(ExecutorService virtualThreadExecutor) {
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

    /**
     * Opens a new request-local fan-out scope.
     *
     * @param maxParallelTasks maximum number of tasks allowed to run at once in this scope
     * @return scope backed by the shared virtual-thread executor
     */
    @Override
    public FanOutScope openScope(int maxParallelTasks) {
        return new VirtualThreadFanOutScope(virtualThreadExecutor, new Semaphore(Math.max(1, maxParallelTasks)));
    }

    /**
     * Concrete fan-out scope backed by one shared semaphore.
     *
     * @param virtualThreadExecutor executor used to start virtual-thread tasks
     * @param requestParallelism request-local capacity limiter
     */
    private record VirtualThreadFanOutScope(
            ExecutorService virtualThreadExecutor,
            Semaphore requestParallelism
    ) implements FanOutScope {

        /**
         * Submits a supplier to run when scope capacity is available.
         *
         * @param supplier task body to execute
         * @param <T> task result type
         * @return joinable task handle
         */
        @Override
        public <T> FanOutTask<T> submit(Supplier<T> supplier) {
            CompletableFuture<T> future = CompletableFuture.supplyAsync(() -> runBounded(requestParallelism, supplier),
                    virtualThreadExecutor);
            return new CompletableFutureFanOutTask<>(future);
        }

        /**
         * Acquires scope capacity, runs the supplier, and always releases capacity.
         *
         * @param semaphore request-local capacity limiter
         * @param supplier task body to execute
         * @param <T> task result type
         * @return supplier result
         */
        private <T> T runBounded(Semaphore semaphore, Supplier<T> supplier) {
            try {
                semaphore.acquire();
                try {
                    return supplier.get();
                } finally {
                    semaphore.release();
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for fan-out capacity", ex);
            }
        }
    }

    /**
     * {@link CompletableFuture}-backed fan-out task handle.
     *
     * @param future future representing the submitted task
     * @param <T> task result type
     */
    private record CompletableFutureFanOutTask<T>(CompletableFuture<T> future) implements FanOutTask<T> {

        /**
         * Waits for completion and unwraps runtime failures for use-case callers.
         *
         * @return task result
         */
        @Override
        public T join() {
            try {
                return future.join();
            } catch (CompletionException ex) {
                if (ex.getCause() instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw ex;
            }
        }
    }
}

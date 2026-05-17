package me.nikonorov.clients.infrastructure.concurrent;

import me.nikonorov.clients.application.fanout.FanOutExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

/**
 * Реализация прикладного контракта fan-out на virtual threads.
 *
 * <p>Каждая отправленная задача выполняется на внедренном executor для virtual
 * threads. Локальный для запроса {@link Semaphore} ограничивает, сколько задач
 * из одной области могут выполняться одновременно.</p>
 */
@Component
class VirtualThreadFanOutExecutor implements FanOutExecutor {

    private final ExecutorService virtualThreadExecutor;

    /**
     * Создает адаптер executor.
     *
     * @param virtualThreadExecutor общий bean executor для virtual-thread-per-task
     */
    VirtualThreadFanOutExecutor(ExecutorService virtualThreadExecutor) {
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

    /**
     * Открывает новую область fan-out, локальную для запроса.
     *
     * @param maxParallelTasks максимальное количество задач, которым разрешено
     *                         одновременно выполняться в этой области
     * @return область на базе общего executor для virtual threads
     */
    @Override
    public FanOutScope openScope(int maxParallelTasks) {
        return new VirtualThreadFanOutScope(virtualThreadExecutor, new Semaphore(Math.max(1, maxParallelTasks)));
    }

    /**
     * Конкретная область fan-out на базе одного общего semaphore.
     *
     * @param virtualThreadExecutor executor для запуска задач на virtual threads
     * @param requestParallelism limiter емкости, локальный для запроса
     */
    private record VirtualThreadFanOutScope(
            ExecutorService virtualThreadExecutor,
            Semaphore requestParallelism
    ) implements FanOutScope {

        /**
         * Отправляет supplier на выполнение, когда доступна емкость области.
         *
         * @param supplier тело выполняемой задачи
         * @param <T> тип результата задачи
         * @return handle задачи, результат которой можно ожидать
         */
        @Override
        public <T> FanOutTask<T> submit(Supplier<T> supplier) {
            CompletableFuture<T> future = CompletableFuture.supplyAsync(() -> runBounded(requestParallelism, supplier),
                    virtualThreadExecutor);
            return new CompletableFutureFanOutTask<>(future);
        }

        /**
         * Захватывает емкость области, выполняет supplier и всегда освобождает емкость.
         *
         * @param semaphore limiter емкости, локальный для запроса
         * @param supplier тело выполняемой задачи
         * @param <T> тип результата задачи
         * @return результат supplier
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
     * Handle fan-out задачи на базе {@link CompletableFuture}.
     *
     * @param future future, представляющий отправленную задачу
     * @param <T> тип результата задачи
     */
    private record CompletableFutureFanOutTask<T>(CompletableFuture<T> future) implements FanOutTask<T> {

        /**
         * Ожидает завершения и разворачивает runtime-ошибки для вызывающих сценариев.
         *
         * @return результат задачи
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

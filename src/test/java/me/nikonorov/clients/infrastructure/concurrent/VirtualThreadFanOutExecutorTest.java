package me.nikonorov.clients.infrastructure.concurrent;

import me.nikonorov.clients.application.FanOutExecutor;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class VirtualThreadFanOutExecutorTest {

    @Test
    void limitsParallelismInsideOneScope() {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            FanOutExecutor fanOutExecutor = new VirtualThreadFanOutExecutor(executor);
            FanOutExecutor.FanOutScope scope = fanOutExecutor.openScope(2);
            AtomicInteger running = new AtomicInteger();
            AtomicInteger maxRunning = new AtomicInteger();
            CountDownLatch allStarted = new CountDownLatch(4);

            List<FanOutExecutor.FanOutTask<Integer>> tasks = new ArrayList<>();
            for (int index = 0; index < 4; index++) {
                tasks.add(scope.submit(() -> {
                    int current = running.incrementAndGet();
                    maxRunning.accumulateAndGet(current, Math::max);
                    allStarted.countDown();
                    sleep(Duration.ofMillis(50));
                    running.decrementAndGet();
                    return current;
                }));
            }

            tasks.forEach(FanOutExecutor.FanOutTask::join);

            assertThat(allStarted.getCount()).isZero();
            assertThat(maxRunning.get()).isLessThanOrEqualTo(2);
        }
    }

    @Test
    void releasesCapacityAfterFailure() {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            FanOutExecutor fanOutExecutor = new VirtualThreadFanOutExecutor(executor);
            FanOutExecutor.FanOutScope scope = fanOutExecutor.openScope(1);

            FanOutExecutor.FanOutTask<String> failed = scope.submit(() -> {
                throw new IllegalStateException("boom");
            });
            FanOutExecutor.FanOutTask<String> next = scope.submit(() -> "ok");

            assertThatThrownBy(failed::join)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("boom");
            assertTimeoutPreemptively(Duration.ofSeconds(1), () -> assertThat(next.join()).isEqualTo("ok"));
        }
    }

    @Test
    void propagatesCriticalFailuresWithoutCompletionExceptionWrapper() {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            FanOutExecutor fanOutExecutor = new VirtualThreadFanOutExecutor(executor);
            FanOutExecutor.FanOutTask<String> failed = fanOutExecutor.openScope(1).submit(() -> {
                throw new UnsupportedOperationException("critical");
            });

            assertThatThrownBy(failed::join)
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("critical");
        }
    }

    @Test
    void restoresInterruptFlagWhenWaitingForCapacityIsInterrupted() throws InterruptedException {
        CapturingExecutorService executor = new CapturingExecutorService();
        FanOutExecutor fanOutExecutor = new VirtualThreadFanOutExecutor(executor);
        FanOutExecutor.FanOutScope scope = fanOutExecutor.openScope(1);
        CountDownLatch firstTaskStarted = new CountDownLatch(1);
        CountDownLatch releaseFirstTask = new CountDownLatch(1);

        scope.submit(() -> {
            firstTaskStarted.countDown();
            await(releaseFirstTask);
            return "first";
        });
        firstTaskStarted.await(1, TimeUnit.SECONDS);

        FanOutExecutor.FanOutTask<String> interrupted = scope.submit(() -> "second");
        Thread waitingThread = executor.secondThread();
        waitingThread.interrupt();

        assertThatThrownBy(interrupted::join)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Interrupted while waiting for fan-out capacity");

        releaseFirstTask.countDown();
        executor.shutdownNow();
    }

    private static void sleep(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
    }

    private static class CapturingExecutorService extends AbstractExecutorService {

        private final List<Thread> threads = new ArrayList<>();
        private final AtomicReference<Runnable> shutdownMarker = new AtomicReference<>();

        @Override
        public void shutdown() {
            shutdownMarker.set(() -> {
            });
        }

        @Override
        public List<Runnable> shutdownNow() {
            threads.forEach(Thread::interrupt);
            shutdown();
            return List.of();
        }

        @Override
        public boolean isShutdown() {
            return shutdownMarker.get() != null;
        }

        @Override
        public boolean isTerminated() {
            return isShutdown();
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) {
            return true;
        }

        @Override
        public void execute(Runnable command) {
            Thread thread = new Thread(command);
            threads.add(thread);
            thread.start();
        }

        Thread secondThread() {
            assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
                while (threads.size() < 2) {
                    Thread.sleep(10);
                }
            });
            return threads.get(1);
        }
    }
}

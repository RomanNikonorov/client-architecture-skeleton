package me.nikonorov.concurrent;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Инфраструктурная конфигурация для выполнения на virtual threads.
 *
 * <p>Bean executor намеренно объявлен в инфраструктурном слое, чтобы прикладные
 * сценарии зависели только от {@link me.nikonorov.fanout.FanOutExecutor}
 * и не могли случайно владеть политикой управления потоками.</p>
 */
@Configuration
class VirtualThreadConfiguration {

    /**
     * Создает общий executor virtual-thread-per-task.
     *
     * <p>Executor закрывается Spring во время shutdown приложения.</p>
     *
     * @return executor, используемый инфраструктурными fan-out компонентами
     */
    @Bean(destroyMethod = "close")
    ExecutorService virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}

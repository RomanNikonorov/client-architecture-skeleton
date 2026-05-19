package me.nikonorov.fanout;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Свойства конфигурации, управляющие прикладным асинхронным fan-out.
 *
 * <p>Значение намеренно ограничено одним запросом. Оно задает, сколько
 * независимых блокирующих операций сценарий использования может одновременно
 * выполнять для одного входящего запроса, не превращаясь в глобальный bulkhead
 * всего сервиса.</p>
 *
 * @param maxParallelTasksPerRequest максимальное количество параллельных задач,
 *                                   разрешенных внутри одной области fan-out
 */
@ConfigurationProperties(prefix = "app.async")
public record AsyncProperties(int maxParallelTasksPerRequest) {

    /**
     * Нормализует некорректные значения конфигурации.
     *
     * <p>Значения меньше {@code 1} приводятся к {@code 1}, потому что область
     * fan-out без доступного слота выполнения привела бы каждый запрос к deadlock.</p>
     */
    public AsyncProperties {
        if (maxParallelTasksPerRequest < 1) {
            maxParallelTasksPerRequest = 1;
        }
    }
}

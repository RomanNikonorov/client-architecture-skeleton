/**
 * Bounded context клиентской агрегации.
 *
 * <p>Пакет организован как кандидат на будущий вынос в отдельный сервис:
 * внутри него находятся собственные входные адаптеры, application слой,
 * доменная модель и infrastructure adapters. Shared-зависимостями остаются
 * только технические {@code fanout} и {@code http} packages.</p>
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Client Aggregation",
        allowedDependencies = {"fanout", "http"}
)
package me.nikonorov.clients;

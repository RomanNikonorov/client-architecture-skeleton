/**
 * Bounded context клиентской агрегации.
 *
 * <p>Пакет организован как кандидат на будущий вынос в отдельный сервис:
 * внутри него находятся собственные входные адаптеры, application слой,
 * доменная модель и infrastructure adapters. Shared-зависимостью остается
 * только технический {@code application.fanout} API.</p>
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Client Aggregation",
        allowedDependencies = "application::fanout"
)
package me.nikonorov.clients.client;

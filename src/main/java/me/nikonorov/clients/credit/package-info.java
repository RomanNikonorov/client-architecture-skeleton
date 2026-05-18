/**
 * Bounded context кредитных решений.
 *
 * <p>Пакет организован как кандидат на будущий вынос в отдельный сервис:
 * внутри него находятся собственные входные адаптеры, application слой,
 * доменная модель и infrastructure adapters. Внешним shared-контрактом остается
 * только технический {@code application.fanout} API.</p>
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Credit",
        allowedDependencies = "application::fanout"
)
package me.nikonorov.clients.credit;

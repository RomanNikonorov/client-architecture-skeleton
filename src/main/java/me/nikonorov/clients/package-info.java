/**
 * Корневой прикладной модуль Spring Modulith для клиентских интеграций.
 *
 * <p>Все пакеты под этим корнем относятся к одному модулю в текущей послойной
 * архитектуре. Зависимости между слоями отдельно проверяются архитектурными
 * тестами.</p>
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Clients",
        allowedDependencies = {}
)
package me.nikonorov.clients;

# ADR 0003: Исходящие интеграции

## Решение

Исходящий gRPC использует blocking stubs. Исходящий REST использует Spring `RestClient`.

Оба подхода безопасны для этого сервиса, потому что блокирующие вызовы выполняются внутри ограниченного fan-out на virtual threads, которым управляют application use cases.

## Правила

- Адрес gRPC-канала и SSL-настройки находятся в `spring.grpc.client.channels.*`.
- REST base URL, client timeouts, общий размер connection pool, per-route connection limit и idle connection eviction находятся в типизированной Spring-конфигурации. Общая transport-модель находится в `http.OutboundRestClientProperties`; `baseUrl` обязателен для каждой внешней REST-системы и читается из настроек, без context-specific defaults в Java-коде.
- Для production REST-клиенты используют shared helper `http.PooledRestClientRequestFactory` поверх Apache HttpClient 5 с отдельным pool на внешнюю систему, eviction expired/idle connections и явным закрытием request factory при остановке context.
- Каждый исходящий REST-клиент создается как именованный bean `RestClient` вместе с отдельным `HttpComponentsClientHttpRequestFactory`, созданным через `PooledRestClientRequestFactory.requestFactory(...)` из типизированных настроек внешней системы.
- Integration-specific `RestClient.Builder` настраивается через `PooledRestClientRequestFactory.restClientBuilder(...)`: helper задает `baseUrl`, `requestFactory` и interceptors конкретной интеграции, созданные приложением.
- Для создания конкретного `RestClient` используется auto-configured bean `RestClient.Builder`, который предоставляет Spring Boot. Configuration method задает integration-specific `baseUrl` и `requestFactory`. Статический `RestClient.builder()` в infrastructure configuration конкретной интеграции не используется, чтобы не обходить Boot customizers, observability и HTTP defaults.
- Критичность, fallback-поведение, mapping и resilience policy находятся в конкретном адаптере.
- `WebClient` не является стандартом по умолчанию для исходящих интеграций.

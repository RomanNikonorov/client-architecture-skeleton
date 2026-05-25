# ADR 0003: Исходящие интеграции

## Решение

Исходящий gRPC использует blocking stubs. Исходящий REST использует Spring `RestClient`.

Оба подхода безопасны для этого сервиса, потому что блокирующие вызовы выполняются внутри ограниченного fan-out на virtual threads, которым управляют application use cases.

## Правила

- Адрес gRPC-канала и SSL-настройки находятся в `spring.grpc.client.channels.*`.
- REST base URL, client timeouts, размер connection pool и idle connection eviction находятся в типизированной Spring-конфигурации. Общая transport-модель находится в `http.OutboundRestClientProperties`, а context-specific default `baseUrl` задается в properties конкретного bounded context.
- Для production REST-клиенты используют shared helper `http.PooledRestClientRequestFactory` поверх Apache HttpClient 5 с отдельным pool на внешнюю систему, eviction expired/idle connections и явным закрытием request factory при остановке context.
- Каждый исходящий REST-клиент создается как именованный bean `RestClient` вместе с отдельным `HttpComponentsClientHttpRequestFactory`, созданным через `PooledRestClientRequestFactory`.
- Для создания конкретного `RestClient` используется shared bean `outboundRestClient` из technical package `http`: configuration method принимает его через `@Qualifier`, вызывает `mutate()`, затем задает `baseUrl` и `requestFactory`. Статический `RestClient.builder()` в infrastructure configuration конкретной интеграции не используется, чтобы не терять observability и tracing.
- Критичность, fallback-поведение, mapping и resilience policy находятся в конкретном адаптере.
- `WebClient` не является стандартом по умолчанию для исходящих интеграций.

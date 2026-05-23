# ADR 0003: Исходящие интеграции

## Решение

Исходящий gRPC использует blocking stubs. Исходящий REST использует Spring `RestClient`.

Оба подхода безопасны для этого сервиса, потому что блокирующие вызовы выполняются внутри ограниченного fan-out на virtual threads, которым управляют application use cases.

## Правила

- Адрес gRPC-канала и SSL-настройки находятся в `spring.grpc.client.channels.*`.
- REST base URL, client timeouts и размер connection pool находятся в типизированной Spring-конфигурации каждого bean `RestClient`.
- Для production REST-клиенты используют shared helper `http.PooledRestClientRequestFactory` поверх Apache HttpClient 5 с отдельным pool на внешнюю систему, eviction expired/idle connections и явным закрытием request factory при остановке context.
- Критичность, fallback-поведение, mapping и resilience policy находятся в конкретном адаптере.
- `WebClient` не является стандартом по умолчанию для исходящих интеграций.

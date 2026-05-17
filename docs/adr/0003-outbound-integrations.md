# ADR 0003: Исходящие интеграции

## Решение

Исходящий gRPC использует blocking stubs. Исходящий REST использует Spring `RestClient`.

Оба подхода безопасны для этого сервиса, потому что блокирующие вызовы выполняются внутри ограниченного fan-out на virtual threads, которым управляют application use cases.

## Правила

- Адрес gRPC-канала и SSL-настройки находятся в `spring.grpc.client.channels.*`.
- REST base URL и client timeouts находятся в Spring-конфигурации каждого bean `RestClient`.
- Критичность, fallback-поведение, mapping и resilience policy находятся в конкретном адаптере.
- `WebClient` не является стандартом по умолчанию для исходящих интеграций.

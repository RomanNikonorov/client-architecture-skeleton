# ADR 0001: Слои и границы модулей

## Решение

Сервис организован вокруг bounded context, который является кандидатом на
будущий вынос в отдельный сервис:

- `clients` содержит клиентскую агрегацию.

Внутри bounded context сохраняется послойная структура:

- `<context>.api.rest` и `<context>.api.grpc` содержат входные адаптеры.
- `<context>.application.usecase` содержит сценарии использования, command/result records и бизнес-оркестрацию.
- `<context>.application.port` содержит прикладные порты к внешним системам.
- `<context>.domain` содержит доменные типы и контракты репозиториев.
- `<context>.infrastructure` содержит JPA, исходящий gRPC, исходящий REST и техническую конфигурацию.

Shared technical packages остаются вне бизнес-доменов:

- `fanout` содержит технический API для ограниченного fan-out внутри use cases.
- `concurrent` содержит реализацию fan-out на virtual threads.

Входные адаптеры должны быть тонкими. Они валидируют и маппят транспортные
запросы, затем вызывают use case своего bounded context.

## Правила

- `<context>.api.*` не должен зависеть от `<context>.infrastructure.*`.
- `<context>.application.*` не должен зависеть от REST, gRPC, JPA или инфраструктурных классов.
- `*.domain.*` не должен зависеть от Spring, JPA, REST, gRPC, API или infrastructure.
- Bounded context не должен импортировать application/domain/infrastructure/API типы другого bounded context, если такой context появляется в проекте.
- Исходящие интеграции доступны только через порты из `<context>.application.port`.
- Бизнес-оркестрация и технический fan-out API не смешиваются в одном package.
- Архитектурные тесты проверяют эти правила.

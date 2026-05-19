# Скелет архитектуры клиентского сервиса

Скелет показывает масштабируемую структуру интеграционного микросервиса с двумя
bounded contexts:

1. `clients` агрегирует клиентский профиль и внешние сигналы.
2. `credit` оценивает кредитное решение и оформлен как отдельный бизнес-домен.
3. REST-точка входа принимает запрос.
4. gRPC-точка входа может принимать тот же сценарий через отдельный входной адаптер.
5. Прикладной слой домена параллельно запускает чтение из БД и исходящие вызовы через `FanOutExecutor`.
6. Параллелизм реализован через virtual threads и ограничен внутри одного запроса.
7. Исходящий gRPC использует blocking stubs, исходящий REST использует Spring `RestClient`.
8. Архитектурные правила зафиксированы в ADR и проверяются тестами.

Оба бизнес-домена имеют собственные слои `api`, `application`, `domain` и
`infrastructure`. Shared-зависимостью остается только технический
`fanout` API и его infrastructure implementation на virtual threads.

## Точка входа

```http
POST /api/v1/clients/aggregate
Content-Type: application/json

{
  "requestId": "req-1",
  "clientId": "client-001"
}
```

Кредитный bounded context:

```http
POST /api/v1/credit/decisions
Content-Type: application/json

{
  "requestId": "req-1",
  "clientId": "client-001",
  "requestedAmount": 300000
}
```

## Слои

- `clients` - bounded context клиентской агрегации.
- `credit` - bounded context кредитных решений.
- `<context>.api.rest` - входные REST-адаптеры домена.
- `<context>.api.grpc` - входные gRPC-адаптеры домена.
- `<context>.application.usecase` - сценарии использования, command/result records и бизнес-оркестрация домена.
- `<context>.application.port` - прикладные порты домена к внешним системам.
- `<context>.domain` - доменная модель и контракты репозиториев домена.
- `<context>.infrastructure` - JPA, исходящие gRPC-клиенты, исходящие REST-клиенты и техническая конфигурация домена.
- `fanout` - shared technical API для ограниченного fan-out внутри use cases.
- `concurrent` - shared implementation fan-out на virtual threads.
- `internal` - детали реализации, которые не должны использоваться другими модулями.

Входные адаптеры должны оставаться тонкими: валидировать и маппить транспортные
запросы, затем вызывать use case своего bounded context. Исходящие интеграции
должны быть скрыты за application-портами этого же bounded context.

Bounded contexts не должны импортировать application/domain/infrastructure/API
типы друг друга. Это сохраняет возможность вынести `clients` или `credit` в
отдельный сервис без распутывания внутренних зависимостей.

## Virtual Threads

Use cases должны использовать `fanout.FanOutExecutor` для ограниченной параллельной работы. Им нельзя создавать или внедрять
`ExecutorService`, `Semaphore`, `Thread` или прямую оркестрацию через `CompletableFuture.supplyAsync`.

Инфраструктурная реализация - `VirtualThreadFanOutExecutor`. Лимит из
`app.async.max-parallel-tasks-per-request` действует на один запрос, а не как глобальный ограничитель всего сервиса.

## Стратегия Runtime

`pom.xml` использует Spring Boot `4.0.6` и Java `25` как runtime/compiler target; preview/incubator API не используются.

## Конфигурация

Исходящие gRPC-каналы:

```yaml
spring:
  grpc:
    client:
      channels:
        system-a:
          address: localhost:9091
        system-b:
          address: localhost:9092
        credit-scoring:
          address: localhost:9093
```

Бизнес-параметры интеграций:

```yaml
app:
  client:
    external-systems:
      system-a:
        channel: system-a
        deadline: 300ms
        critical: true
        circuit-breaker-enabled: false
      system-b:
        channel: system-b
        deadline: 500ms
        critical: false
        circuit-breaker-enabled: true
```

Исходящие REST-клиенты:

```yaml
app:
  client:
    external-rest-systems:
      system-c:
        base-url: http://localhost:9083
        connect-timeout: 300ms
        read-timeout: 500ms
        critical: false
```

Кредитный bounded context использует отдельные настройки под `app.credit`:

```yaml
app:
  credit:
    external-systems:
      scoring:
        channel: credit-scoring
        deadline: 400ms
        critical: false
    external-rest-systems:
      pricing:
        base-url: http://localhost:9084
        connect-timeout: 300ms
        read-timeout: 500ms
        critical: false
```

## Архитектурные решения

- `docs/adr/0001-layering-and-module-boundaries.md`
- `docs/adr/0002-virtual-threads-and-fan-out.md`
- `docs/adr/0003-outbound-integrations.md`
- `docs/adr/0004-testing-and-architecture-enforcement.md`

## Проверка

```bash
mvn test
```

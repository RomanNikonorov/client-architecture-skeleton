# Скелет архитектуры клиентского сервиса

Скелет показывает масштабируемую структуру интеграционного микросервиса:

1. REST-точка входа принимает запрос.
2. gRPC-точка входа может принимать тот же сценарий через отдельный входной адаптер.
3. Прикладной слой параллельно запускает чтение из БД и исходящие вызовы через `FanOutExecutor`.
4. Параллелизм реализован через virtual threads и ограничен внутри одного запроса.
5. Исходящий gRPC использует blocking stubs, исходящий REST использует Spring `RestClient`.
6. Архитектурные правила зафиксированы в ADR и проверяются тестами.

## Точка входа

```http
POST /api/v1/clients/aggregate
Content-Type: application/json

{
  "requestId": "req-1",
  "clientId": "client-001"
}
```

## Слои

- `api.rest` - входные REST-адаптеры.
- `api.grpc` - входные gRPC-адаптеры.
- `application.usecase` - сценарии использования, command/result records и бизнес-оркестрация.
- `application.port` - прикладные порты к внешним системам.
- `application.fanout` - технический API для ограниченного fan-out внутри use cases.
- `domain` - доменная модель и контракт репозитория.
- `infrastructure` - JPA, исходящие gRPC-клиенты, исходящие REST-клиенты и техническая конфигурация.
- `internal` - детали реализации, которые не должны использоваться другими модулями.

Входные адаптеры должны оставаться тонкими: валидировать и маппить транспортные запросы, затем вызывать `application.usecase`.
Исходящие интеграции должны быть скрыты за портами из `application.port`.

## Virtual Threads

Use cases должны использовать `application.fanout.FanOutExecutor` для ограниченной параллельной работы. Им нельзя создавать или внедрять
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
```

Бизнес-параметры интеграций:

```yaml
app:
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
  external-rest-systems:
    system-c:
      base-url: http://localhost:9083
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

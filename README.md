# Скелет архитектуры клиентского сервиса

Скелет показывает масштабируемую структуру интеграционного микросервиса с
bounded context клиентской агрегации:

1. `clients` агрегирует клиентский профиль и внешние сигналы.
2. REST-точка входа принимает запрос.
3. gRPC-точка входа может принимать тот же сценарий через отдельный входной адаптер.
4. Прикладной слой домена параллельно запускает чтение из БД и исходящие вызовы через `FanOutExecutor`.
5. Параллелизм реализован через virtual threads и ограничен внутри одного запроса.
6. Исходящий gRPC использует blocking stubs, исходящий REST использует Spring `RestClient`.
7. Архитектурные правила зафиксированы в ADR и проверяются тестами.

Бизнес-домен имеет собственные слои `api`, `application`, `domain` и
`infrastructure`. Shared-зависимостями остаются технический `fanout` API, его
infrastructure implementation на virtual threads и HTTP helpers.

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

- `clients` - bounded context клиентской агрегации.
- `<context>.api.rest` - входные REST-адаптеры домена.
- `<context>.api.grpc` - входные gRPC-адаптеры домена.
- `<context>.application.usecase` - сценарии использования, command/result records и бизнес-оркестрация домена.
- `<context>.application.port` - прикладные порты домена к внешним системам.
- `<context>.domain` - доменная модель и контракты репозиториев домена.
- `<context>.infrastructure` - JPA, исходящие gRPC-клиенты, исходящие REST-клиенты и техническая конфигурация домена.
- `fanout` - shared technical API для ограниченного fan-out внутри use cases.
- `concurrent` - shared implementation fan-out на virtual threads.
- `http` - shared technical helpers для исходящих REST-клиентов infrastructure слоя.
- `internal` - детали реализации, которые не должны использоваться другими модулями.

Входные адаптеры должны оставаться тонкими: валидировать и маппить транспортные
запросы, затем вызывать use case своего bounded context. Исходящие интеграции
должны быть скрыты за application-портами этого же bounded context.

Bounded contexts не должны импортировать application/domain/infrastructure/API
типы друг друга. Это сохраняет возможность вынести домен в отдельный сервис без
распутывания внутренних зависимостей.

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
        pool-size: 20
        max-connections-per-route: 20
        idle-connection-eviction-timeout: 30s
        critical: false
```

### Конфигурирование исходящего REST-клиента

REST-интеграция настраивается в три шага:

1. YAML-блок конкретной внешней системы биндингом попадает в properties
   bounded context.
2. Общие HTTP-параметры хранятся в `http.OutboundRestClientProperties`.
3. Infrastructure configuration создает именованный `RestClient` и отдельный
   `HttpComponentsClientHttpRequestFactory` через `http.PooledRestClientRequestFactory`.

Например, для `clients` YAML:

```yaml
app:
  client:
    external-rest-systems:
      system-c:
        base-url: http://localhost:9083
        connect-timeout: 300ms
        read-timeout: 500ms
        pool-size: 20
        max-connections-per-route: 20
        idle-connection-eviction-timeout: 30s
        critical: false
```

биндится в:

```java
@ConfigurationProperties(prefix = "app.client.external-rest-systems")
public record ExternalRestSystemsProperties(OutboundRestClientProperties systemC) {
}
```

`OutboundRestClientProperties` содержит transport-настройки:

- `baseUrl` - базовый URL внешней REST-системы;
- `connectTimeout` - timeout на установку HTTP-соединения;
- `readTimeout` - timeout на чтение HTTP-ответа;
- `poolSize` - максимальный размер connection pool;
- `maxConnectionsPerRoute` - максимальный размер connection pool на один route;
- `idleConnectionEvictionTimeout` - время простоя соединения перед eviction;
- `critical` - признак, должен ли adapter пробрасывать ошибки вместо fallback.

Общие HTTP defaults задаются один раз в `OutboundRestClientProperties`:

| Параметр | Default |
| --- | --- |
| `connect-timeout` | `300ms` |
| `read-timeout` | `500ms` |
| `pool-size` | `20` |
| `max-connections-per-route` | значение `pool-size` |
| `idle-connection-eviction-timeout` | `30s` |

Default `base-url` зависит от конкретной внешней системы и задается в properties
bounded context. Например, `ExternalRestSystemsProperties` подставляет
`http://localhost:9083` для `system-c`. Любой параметр можно переопределить
через YAML.

Infrastructure configuration должна создавать два bean на внешнюю систему.
Именованный `RestClient` создается через auto-configured `RestClient.Builder`,
который предоставляет Spring Boot:

```java
@Bean("externalSystemCRestApiClient")
RestClient externalSystemCRestApiClient(
        RestClient.Builder restClientBuilder,
        @Qualifier("externalSystemCRequestFactory")
        HttpComponentsClientHttpRequestFactory requestFactory,
        ExternalRestSystemsProperties properties
) {
    return restClientBuilder
            .baseUrl(properties.systemC().baseUrl().toString())
            .requestFactory(requestFactory)
            .build();
}

@Bean(destroyMethod = "destroy")
HttpComponentsClientHttpRequestFactory externalSystemCRequestFactory(
        ExternalRestSystemsProperties properties
) {
    return PooledRestClientRequestFactory.requestFactory(properties.systemC());
}
```

Adapter внедряет именно именованный `RestClient`:

```java
ExternalSystemCRestClient(
        @Qualifier("externalSystemCRestApiClient")
        RestClient externalSystemCRestClient,
        ExternalRestSystemsProperties properties
) {
    this.externalSystemCRestClient = externalSystemCRestClient;
    this.properties = properties;
}
```

Для новой REST-интеграции в существующем bounded context нужно:

1. Добавить поле `OutboundRestClientProperties` в properties этого context.
2. В compact constructor properties подставить default `baseUrl` через
   `OutboundRestClientProperties.withDefaultHttpParameters(...)` или
   `withDefaultBaseUrl(...)`.
3. Добавить именованные beans `RestClient` и request factory в
   `<context>.infrastructure.rest`; `RestClient` создавать через
   auto-configured `RestClient.Builder`, а не через статический
   `RestClient.builder()`.
4. В adapter внедрить `RestClient` через `@Qualifier`.
5. Покрыть adapter mapping/fallback behavior тестом.

## Архитектурные решения

- `docs/adr/0001-layering-and-module-boundaries.md`
- `docs/adr/0002-virtual-threads-and-fan-out.md`
- `docs/adr/0003-outbound-integrations.md`
- `docs/adr/0004-testing-and-architecture-enforcement.md`

## Проверка

```bash
mvn test
```

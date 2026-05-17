# Client Architecture Skeleton

Скелет показывает масштабируемую структуру интеграционного микросервиса:

1. REST endpoint принимает запрос.
2. gRPC endpoint может принимать тот же сценарий через отдельный inbound adapter.
3. Application слой параллельно запускает чтение из БД и outbound вызовы через `FanOutExecutor`.
4. Параллелизм реализован через virtual threads и ограничен внутри одного запроса.
5. Outbound gRPC использует blocking stubs, outbound REST использует Spring `RestClient`.
6. Архитектурные правила зафиксированы в ADR и проверяются тестами.

## Endpoint

```http
POST /api/v1/clients/aggregate
Content-Type: application/json

{
  "requestId": "req-1",
  "clientId": "client-001"
}
```

## Layering

- `api.rest` - REST входные адаптеры.
- `api.grpc` - gRPC входные адаптеры.
- `application` - use cases, orchestration, ports to external systems.
- `domain` - domain model and repository contract.
- `infrastructure` - JPA, outbound gRPC clients, technical configuration.
- `internal` - implementation details that should not be used by other modules.

Inbound adapters must stay thin: validate/map transport requests and call application use cases.
Outbound integrations must be hidden behind application ports.

## Virtual Threads

Use cases must use `FanOutExecutor` for bounded parallel work. They must not create or inject
`ExecutorService`, `Semaphore`, `Thread`, or direct `CompletableFuture.supplyAsync` orchestration.

The infrastructure implementation is `VirtualThreadFanOutExecutor`. The limit configured by
`app.async.max-parallel-tasks-per-request` is per request, not a global bulkhead.

## Runtime Strategy

`pom.xml` использует Spring Boot `4.0.6`, Java `25` как runtime/compiler target, and no preview/incubator APIs.

## Configuration

Outbound gRPC channels:

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

Business-level integration parameters:

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

Outbound REST clients:

```yaml
app:
  external-rest-systems:
    system-c:
      base-url: http://localhost:9083
      connect-timeout: 300ms
      read-timeout: 500ms
      critical: false
```

## Architecture Decisions

- `docs/adr/0001-layering-and-module-boundaries.md`
- `docs/adr/0002-virtual-threads-and-fan-out.md`
- `docs/adr/0003-outbound-integrations.md`
- `docs/adr/0004-testing-and-architecture-enforcement.md`

## Verification

```bash
mvn test
```

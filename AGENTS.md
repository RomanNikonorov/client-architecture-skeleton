# AGENTS.md

Правила для AI-агентов и разработчиков, которые меняют этот проект.

## Быстрый контекст

Это интеграционный микросервис на Spring Boot 4 и Java 25. Проект показывает масштабируемую послойную архитектуру для десятков REST/gRPC endpoint и внешних интеграций по gRPC/REST.

Перед изменениями прочитай:

- `README.md` - краткая карта проекта.
- `docs/adr/0001-layering-and-module-boundaries.md` - слои и границы.
- `docs/adr/0002-virtual-threads-and-fan-out.md` - правила virtual threads.
- `docs/adr/0003-outbound-integrations.md` - правила исходящих gRPC/REST интеграций.
- `docs/adr/0004-testing-and-architecture-enforcement.md` - обязательные проверки.

Главный критерий готовности: после изменений должен проходить `mvn test`.

## Базовые команды

```bash
mvn test
```

Проект не обязан быть git-репозиторием в локальной папке. Не полагайся на `git diff` как на единственный способ проверки результата.

## Package и структура

Корневой package приложения:

```text
me.nikonorov.clients
```

Используй только этот корень для production- и test-кода. Не добавляй новые классы под `com.example`.

Основные слои:

- `api.rest` - входные REST-адаптеры.
- `api.grpc` - входные gRPC-адаптеры.
- `application.usecase` - сценарии использования, command/result records и бизнес-оркестрация.
- `application.port` - прикладные порты к внешним системам.
- `application.fanout` - технический API для ограниченного fan-out внутри сценариев использования.
- `domain` - доменные типы и контракты репозиториев.
- `infrastructure` - JPA, исходящие gRPC/REST адаптеры, техническая конфигурация.
- `internal` - детали реализации, которые не должны использоваться другими модулями.

Правила зависимостей:

- `api.*` не зависит от `infrastructure.*`.
- `application.*` не зависит от `api.*`, `infrastructure.*`, REST, gRPC, JPA и сгенерированных классов.
- `domain.*` не зависит от Spring, JPA, REST, gRPC и infrastructure.
- Исходящие интеграции доступны прикладному слою только через порты из `application.port`.
- Входные адаптеры вызывают сценарии использования, а не infrastructure напрямую.

Если нужно нарушить правило, сначала добавь или измени ADR, затем обнови архитектурные тесты. Не оставляй нарушение только как "временное".

## Входные REST-точки

REST-код размещай в:

```text
src/main/java/me/nikonorov/clients/api/rest
```

REST controller должен быть тонким:

- принимает и валидирует транспортный DTO;
- маппит DTO в прикладную команду;
- вызывает сценарий использования;
- маппит прикладной результат в транспортный ответ, если отдельный response DTO нужен.

Нельзя:

- внедрять JPA repositories, gRPC stubs, `RestClient` или infrastructure adapters в REST controller;
- размещать бизнес-оркестрацию в controller;
- возвращать infrastructure или сгенерированные типы наружу без явного решения.

Для новой REST-точки добавь тест адаптера рядом с существующим примером в `src/test/java/me/nikonorov/clients/api/rest`.

## Входные gRPC-точки

gRPC API-контракты размещай в:

```text
src/main/proto
```

Для входящего gRPC используй отдельный `java_package` под API-слоем:

```proto
option java_package = "me.nikonorov.clients.api.grpc.generated";
```

gRPC service размещай в:

```text
src/main/java/me/nikonorov/clients/api/grpc
```

gRPC service должен быть тонким:

- принимает сгенерированный request;
- маппит его в прикладную команду;
- вызывает сценарий использования;
- маппит прикладной результат в сгенерированный response;
- завершает `StreamObserver`.

Нельзя:

- импортировать infrastructure-классы в `api.grpc`;
- выполнять исходящие REST/gRPC вызовы из gRPC service;
- помещать бизнес-оркестрацию в gRPC service.

Для новой gRPC-точки добавь тест адаптера в `src/test/java/me/nikonorov/clients/api/grpc`.

## Application слой

Прикладной слой разделен на три подпакета:

- `application.usecase` - бизнес-сценарии, command/result records, orchestration;
- `application.port` - интерфейсы исходящих портов;
- `application.fanout` - технический API fan-out и его configuration properties.

Не смешивай бизнес-оркестрацию и fan-out технику в одном package. `FanOutExecutor` не должен лежать рядом с use cases.

Сценарий использования должен:

- быть транспортно-независимым;
- зависеть от доменных контрактов и прикладных портов;
- использовать `FanOutExecutor` для параллельной работы;
- не знать про REST DTO, сгенерированные gRPC messages, JPA entities, `RestClient`, gRPC stubs.

Нельзя в сценариях использования:

- создавать или внедрять `ExecutorService`;
- создавать `Semaphore`;
- создавать `Thread`;
- вызывать `CompletableFuture.supplyAsync`;
- импортировать `java.util.concurrent.*` для ручной оркестрации.

Если сценарию использования нужна параллельная работа, открывай scope:

```java
FanOutExecutor.FanOutScope fanOut = fanOutExecutor.openScope(maxParallelTasks);
```

Затем отправляй связанные задачи в этот scope и ожидай их через `FanOutTask`.

## Virtual Threads и Fan-Out

Virtual threads принадлежат infrastructure слою.

Текущая реализация:

- прикладной технический API: `application.fanout.FanOutExecutor`;
- infrastructure implementation: `VirtualThreadFanOutExecutor`;
- executor bean: `VirtualThreadConfiguration`;
- лимит: `app.async.max-parallel-tasks-per-request`.

Семантика лимита:

- лимит действует внутри одного fan-out scope;
- scope обычно соответствует одному входящему запросу или одной прикладной команде;
- это не глобальный bulkhead всего сервиса.

При изменении fan-out поведения обязательно обнови тесты:

- лимит параллелизма внутри одного scope;
- освобождение capacity после успеха;
- освобождение capacity после ошибки;
- проброс runtime-ошибок без лишней обертки для сценария использования;
- восстановление interrupt flag при прерывании ожидания capacity.

## Domain слой

Domain слой содержит только доменную модель и контракты.

Можно:

- records/value objects;
- доменные интерфейсы репозиториев;
- чистую доменную логику без Spring/JPA/gRPC/REST.

Нельзя:

- JPA annotations;
- Spring annotations;
- сгенерированные gRPC classes;
- REST DTO;
- infrastructure exceptions, если они привязывают domain к адаптеру.

## Infrastructure: БД

JPA-код размещай в:

```text
src/main/java/me/nikonorov/clients/infrastructure/db
```

Паттерн:

- JPA entity package-private;
- Spring Data repository package-private;
- adapter реализует доменный контракт репозитория;
- adapter маппит entity в доменный тип.

Прикладной слой не должен видеть JPA entity и Spring Data repository.

## Infrastructure: исходящий gRPC

Исходящие gRPC proto для внешних систем должны генерироваться в infrastructure package:

```proto
option java_package = "me.nikonorov.clients.infrastructure.grpc.generated";
```

Исходящие gRPC adapters размещай в:

```text
src/main/java/me/nikonorov/clients/infrastructure/grpc
```

Паттерн адаптера:

- implements прикладной port;
- принимает сгенерированный blocking stub;
- строит сгенерированный request;
- задает deadline через конфигурацию;
- вызывает blocking stub;
- маппит сгенерированный response в прикладной result type;
- применяет resilience/fallback policy внутри adapter.

Адреса каналов и SSL:

```yaml
spring:
  grpc:
    client:
      channels:
        system-a:
          address: localhost:9091
```

Бизнес-параметры интеграции держи в типизированных `@ConfigurationProperties`, например `ExternalSystemsProperties`.

## Infrastructure: исходящий REST

Исходящие REST adapters размещай в:

```text
src/main/java/me/nikonorov/clients/infrastructure/rest
```

Стандарт по умолчанию: Spring `RestClient`.

Паттерн адаптера:

- implements прикладной port;
- принимает именованный или типизированный `RestClient` bean;
- строит URI;
- вызывает внешний REST API блокирующим способом;
- маппит response DTO в прикладной result type;
- применяет fallback/critical behavior внутри adapter.

Нельзя использовать `WebClient` как стандарт по умолчанию. Если он действительно нужен, сначала зафиксируй причину в ADR.

REST base URL и timeouts настраивай через типизированные properties и `RestClientConfiguration`.

## Ошибки, critical и fallback

Общее правило:

- critical integration failures пробрасываются;
- non-critical integration failures маппятся в нормализованный unavailable/fallback результат;
- решение о critical/fallback принадлежит конкретному adapter или сценарию использования, а не транспортному controller/service.

Для текущей модели внешнего сигнала используй:

```java
ClientAggregationResult.ExternalSignal.unavailable(source, reason)
```

Не скрывай ошибки без предупреждения, видимого вызывающей стороне, или явно нормализованного результата.

## Конфигурация

Используй типизированную конфигурацию:

- `@ConfigurationProperties` для групп настроек;
- records для неизменяемой конфигурации;
- нормализацию опасных значений в compact constructor, если нужно.

Не читай сырые значения окружения напрямую из бизнес-кода.

## Тесты и архитектурный контроль

Обязательные тесты для новых изменений:

- unit test сценария использования для оркестрации;
- adapter test для REST/gRPC mapping;
- тест fallback/critical behavior для новых внешних интеграций;
- архитектурные тесты должны продолжать проходить.

Существующие архитектурные тесты:

```text
src/test/java/me/nikonorov/clients/architecture/ArchitectureRulesTest.java
src/test/java/me/nikonorov/clients/ModulithStructureTest.java
```

Если добавляешь новый слой, пакет или исключение из правил, обнови тесты и ADR вместе.

## Документация и комментарии

Документация проекта ведется на русском языке:

- `README.md`;
- `docs/adr/*.md`;
- Javadoc в production Java-коде;
- `AGENTS.md`.

Технические идентификаторы не переводятся:

- имена классов и методов;
- package names;
- configuration keys;
- protocol names вроде REST/gRPC;
- Spring/Maven dependency names.

Для новых public или архитектурно важных классов добавляй Javadoc. Комментарии должны объяснять назначение, границы ответственности и правила использования, а не пересказывать очевидный код.

## Работа с generated-кодом

Не редактируй сгенерированные protobuf sources в `target/generated-sources`.

Изменяй `.proto` файлы в `src/main/proto`, затем проверяй сборку через:

```bash
mvn test
```

Сгенерированные packages должны соответствовать слою:

- входящий API: `me.nikonorov.clients.api.grpc.generated`;
- исходящие внешние системы: `me.nikonorov.clients.infrastructure.grpc.generated`.

## Runtime и зависимости

Текущая целевая версия:

- Spring Boot `4.0.6`;
- Java `25`;
- preview/incubator API не используются.

Не добавляй новые зависимости без причины. Если зависимость нужна:

- объясни, какой архитектурный или технический пробел она закрывает;
- проверь, что существующий Spring/Maven stack не решает задачу уже сейчас;
- добавь тест или пример использования.

## Критерии готовности изменения

Перед завершением работы проверь:

- код лежит в правильном слое;
- прикладной слой не зависит от adapter/infrastructure технологий;
- сценарии использования применяют `application.fanout.FanOutExecutor`, если нужна параллельность;
- новые интеграции закрыты прикладным портом;
- добавлены или обновлены релевантные тесты;
- документация/ADR обновлены, если изменилось архитектурное правило;
- `mvn test` проходит.

## Частые ошибки

Не делай так:

- REST controller вызывает `RestClient`, gRPC stub или JPA repository напрямую.
- gRPC service содержит бизнес-оркестрацию.
- Сценарий использования создает `CompletableFuture.supplyAsync` или `Semaphore`.
- Прикладной порт возвращает сгенерированное gRPC message.
- Доменный тип содержит JPA annotation.
- REST-интеграция реализована через `WebClient` без ADR.
- Новая внешняя система добавлена без типизированной конфигурации и теста adapter mapping.

Делай так:

- transport adapter -> application command -> use case -> application ports -> infrastructure adapters;
- blocking I/O в fan-out выполняется через `FanOutExecutor`;
- ошибки внешних систем нормализуются или пробрасываются по явно заданной critical policy;
- архитектурные правила закрепляются тестами.

# ADR 0002: Virtual Threads и Fan-Out

## Решение

Вся ограниченная fan-out параллельность внутри use cases проходит через `fanout.FanOutExecutor`.

Use cases не должны создавать или внедрять `ExecutorService`, `Semaphore`, `Thread` или прямую оркестрацию через `CompletableFuture.supplyAsync`. Executor на virtual threads принадлежит infrastructure слою.

## Правила

- `FanOutExecutor` находится в `fanout` и является техническим прикладным API.
- `VirtualThreadFanOutExecutor` - инфраструктурная реализация.
- Fan-out лимиты действуют на один запрос, а не как глобальный ограничитель всего сервиса.
- Прерванное ожидание восстанавливает interrupt flag и завершается явной ошибкой.
- Архитектурные тесты запрещают прямое владение concurrency primitives в use cases.

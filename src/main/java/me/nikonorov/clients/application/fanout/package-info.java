/**
 * Публичный интерфейс application модуля с техническим API fan-out.
 *
 * <p>Use cases используют эти типы для ограниченной параллельной работы, а
 * infrastructure слой предоставляет реализацию на virtual threads.</p>
 */
@org.springframework.modulith.NamedInterface("fanout")
package me.nikonorov.clients.application.fanout;

/**
 * Публичный интерфейс модуля с техническим API fan-out.
 *
 * <p>Use cases используют эти типы для ограниченной параллельной работы, а
 * infrastructure слой предоставляет реализацию на virtual threads.</p>
 */
package me.nikonorov.fanout;

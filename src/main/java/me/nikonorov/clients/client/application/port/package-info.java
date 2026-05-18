/**
 * Публичный интерфейс application модуля с исходящими портами.
 *
 * <p>Infrastructure adapters реализуют эти порты, а use cases вызывают внешние
 * системы только через них.</p>
 */
@org.springframework.modulith.NamedInterface("port")
package me.nikonorov.clients.client.application.port;

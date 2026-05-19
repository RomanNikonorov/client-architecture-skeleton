/**
 * Публичный интерфейс application модуля со сценариями использования.
 *
 * <p>Входные REST/gRPC адаптеры зависят от этих типов, чтобы запускать
 * бизнес-оркестрацию без доступа к infrastructure слою.</p>
 */
@org.springframework.modulith.NamedInterface("usecase")
package me.nikonorov.clients.application.usecase;

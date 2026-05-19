/**
 * Входные адаптеры кредитного bounded context.
 *
 * <p>REST и gRPC adapters принимают транспортные запросы, преобразуют их в
 * прикладные команды и вызывают use cases. Этот слой не обращается напрямую к
 * infrastructure adapters или внешним системам.</p>
 */
package me.nikonorov.credit.api;

package me.nikonorov.clients;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Точка входа Spring Boot для интеграционного сервиса клиентов.
 *
 * <p>Приложение сканирует конфигурационные свойства и component beans под
 * {@code me.nikonorov.clients}. Новые REST, gRPC, прикладные, доменные и
 * инфраструктурные классы должны оставаться под этим корневым пакетом, чтобы
 * Spring и Spring Modulith находили их единообразно.</p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ClientArchitectureSkeletonApplication {

    /**
     * Запускает Spring Boot приложение.
     *
     * @param args аргументы командной строки, переданные средой выполнения
     */
    public static void main(String[] args) {
        SpringApplication.run(ClientArchitectureSkeletonApplication.class, args);
    }
}

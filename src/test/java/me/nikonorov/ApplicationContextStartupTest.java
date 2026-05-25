package me.nikonorov;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke-тест полного Spring context.
 *
 * <p>Проверяет, что application wiring поднимается целиком, включая REST/gRPC
 * infrastructure beans, которые не покрываются узкими unit-тестами адаптеров.
 * </p>
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.grpc.server.port=0"
)
class ApplicationContextStartupTest {

    @Test
    void startsApplicationContext() {
    }
}

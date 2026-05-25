package me.nikonorov.http;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PooledRestClientRequestFactoryTest {

    @Test
    void createsRequestFactoryFromProperties() {
        OutboundRestClientProperties properties = new OutboundRestClientProperties(
                URI.create("http://localhost:9083"),
                Duration.ofMillis(300),
                Duration.ofMillis(500),
                7,
                3,
                Duration.ofSeconds(30),
                false
        );

        HttpComponentsClientHttpRequestFactory requestFactory =
                PooledRestClientRequestFactory.requestFactory(properties);

        assertThat(requestFactory.getHttpClient()).isNotNull();
    }

    @Test
    void configuresRestClientBuilderWithLoggerAndAdditionalInterceptors() {
        OutboundRestClientProperties properties = new OutboundRestClientProperties(
                URI.create("http://localhost:9083"),
                Duration.ofMillis(300),
                Duration.ofMillis(500),
                7,
                3,
                Duration.ofSeconds(30),
                false
        );
        AtomicInteger loggingCalls = new AtomicInteger();
        AtomicInteger additionalInterceptorCalls = new AtomicInteger();
        ClientHttpRequestInterceptor loggingInterceptor = (request, body, execution) -> {
            loggingCalls.incrementAndGet();
            return execution.execute(request, body);
        };
        ClientHttpRequestInterceptor additionalInterceptor = (request, body, execution) -> {
            additionalInterceptorCalls.incrementAndGet();
            request.getHeaders().add("X-Client-Trace", "system-c");
            return execution.execute(request, body);
        };

        RestClient.Builder restClientBuilder = PooledRestClientRequestFactory.restClientBuilder(
                RestClient.builder(),
                properties,
                new SimpleClientHttpRequestFactory(),
                loggingInterceptor,
                additionalInterceptor
        );
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        RestClient restClient = restClientBuilder.build();

        server.expect(requestTo("http://localhost:9083/ping"))
                .andExpect(header("X-Client-Trace", "system-c"))
                .andRespond(withSuccess("ok", MediaType.TEXT_PLAIN));

        String response = restClient.get()
                .uri("/ping")
                .retrieve()
                .body(String.class);

        assertThat(response).isEqualTo("ok");
        assertThat(loggingCalls).hasValue(1);
        assertThat(additionalInterceptorCalls).hasValue(1);
        server.verify();
    }

    @Test
    void exposesOnlyRequestFactoryAsPublicFactoryMethod() {
        assertThat(Arrays.stream(PooledRestClientRequestFactory.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .map(Method::getName))
                .containsExactlyInAnyOrder("requestFactory", "restClientBuilder");
    }
}

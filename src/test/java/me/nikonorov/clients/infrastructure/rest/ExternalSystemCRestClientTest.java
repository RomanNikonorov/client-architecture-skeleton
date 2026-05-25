package me.nikonorov.clients.infrastructure.rest;

import me.nikonorov.clients.application.usecase.ClientAggregationCommand;
import me.nikonorov.clients.application.usecase.ClientAggregationResult;
import me.nikonorov.http.OutboundRestClientProperties;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalSystemCRestClientTest {

    @Test
    void mapsRestResponseToExternalSignal() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/clients/client-001/signal", exchange -> {
            byte[] body = "{\"status\":\"OK\",\"value\":\"rest-signal\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        try {
            URI baseUrl = URI.create("http://localhost:" + server.getAddress().getPort());
            ExternalRestSystemsProperties properties = new ExternalRestSystemsProperties(
                    new OutboundRestClientProperties(
                            baseUrl,
                            Duration.ofMillis(300),
                            Duration.ofMillis(500),
                            20,
                            20,
                            Duration.ofSeconds(30),
                            false
                    )
            );
            ExternalSystemCRestClient client = new ExternalSystemCRestClient(
                    RestClient.builder().baseUrl(baseUrl.toString()).build(),
                    properties
            );

            ClientAggregationResult.ExternalSignal signal = client.getClientSignal(
                    new ClientAggregationCommand("req-1", "client-001"));

            assertThat(signal.source()).isEqualTo("system-c");
            assertThat(signal.status()).isEqualTo("OK");
            assertThat(signal.value()).isEqualTo("rest-signal");
        } finally {
            server.stop(0);
        }
    }
}

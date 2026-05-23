package me.nikonorov.credit.infrastructure.rest;

import com.sun.net.httpserver.HttpServer;
import me.nikonorov.credit.application.usecase.CreditDecisionCommand;
import me.nikonorov.credit.application.usecase.CreditDecisionResult;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class CreditPricingRestClientTest {

    @Test
    void mapsRestResponseToPricingOffer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/credit/pricing/client-001", exchange -> {
            byte[] body = "{\"status\":\"OK\",\"ratePlan\":\"prime\",\"annualRateBasisPoints\":1490}"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        try {
            URI baseUrl = URI.create("http://localhost:" + server.getAddress().getPort());
            CreditRestSystemsProperties properties = new CreditRestSystemsProperties(
                    new CreditRestSystemsProperties.SystemConfig(
                            baseUrl,
                            Duration.ofMillis(300),
                            Duration.ofMillis(500),
                            20,
                            false
                    )
            );
            CreditPricingRestClient client = new CreditPricingRestClient(
                    RestClient.builder().baseUrl(baseUrl.toString()).build(),
                    properties
            );

            CreditDecisionResult.PricingOffer offer = client.quote(
                    new CreditDecisionCommand("req-1", "client-001", 300000));

            assertThat(offer.source()).isEqualTo("credit-pricing");
            assertThat(offer.status()).isEqualTo("OK");
            assertThat(offer.ratePlan()).isEqualTo("prime");
            assertThat(offer.annualRateBasisPoints()).isEqualTo(1490);
        } finally {
            server.stop(0);
        }
    }
}

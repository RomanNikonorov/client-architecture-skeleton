package me.nikonorov.clients.credit.infrastructure.grpc;

import me.nikonorov.clients.credit.infrastructure.grpc.generated.CreditScoringGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

/**
 * Инфраструктурная конфигурация исходящих gRPC stubs кредитного bounded context.
 */
@Configuration
class CreditGrpcClientConfiguration {

    /**
     * Создает blocking stub, используемый адаптером scoring.
     *
     * @param channels Spring gRPC channel factory
     * @param properties конфигурация scoring-системы с именем channel
     * @return сгенерированный blocking stub для scoring
     */
    @Bean
    CreditScoringGrpc.CreditScoringBlockingStub creditScoringStub(
            GrpcChannelFactory channels,
            CreditExternalSystemsProperties properties
    ) {
        return CreditScoringGrpc.newBlockingStub(channels.createChannel(properties.scoring().channel()));
    }
}

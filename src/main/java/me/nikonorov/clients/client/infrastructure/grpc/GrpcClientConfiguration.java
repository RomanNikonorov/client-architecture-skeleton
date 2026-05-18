package me.nikonorov.clients.client.infrastructure.grpc;

import me.nikonorov.clients.client.infrastructure.grpc.generated.ExternalSystemAGrpc;
import me.nikonorov.clients.client.infrastructure.grpc.generated.ExternalSystemBGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

/**
 * Инфраструктурная конфигурация для сгенерированных исходящих gRPC stubs.
 *
 * <p>Каждый bean строится из именованного Spring gRPC channel. Адреса каналов
 * и TLS-настройки задаются в {@code spring.grpc.client.channels.*}.</p>
 */
@Configuration
class GrpcClientConfiguration {

    /**
     * Создает blocking stub, используемый адаптером system A.
     *
     * @param channels Spring gRPC channel factory
     * @param properties конфигурация внешней системы с именем channel
     * @return сгенерированный blocking stub для system A
     */
    @Bean
    ExternalSystemAGrpc.ExternalSystemABlockingStub externalSystemAStub(
            GrpcChannelFactory channels,
            ExternalSystemsProperties properties
    ) {
        return ExternalSystemAGrpc.newBlockingStub(channels.createChannel(properties.systemA().channel()));
    }

    /**
     * Создает blocking stub, используемый адаптером system B.
     *
     * @param channels Spring gRPC channel factory
     * @param properties конфигурация внешней системы с именем channel
     * @return сгенерированный blocking stub для system B
     */
    @Bean
    ExternalSystemBGrpc.ExternalSystemBBlockingStub externalSystemBStub(
            GrpcChannelFactory channels,
            ExternalSystemsProperties properties
    ) {
        return ExternalSystemBGrpc.newBlockingStub(channels.createChannel(properties.systemB().channel()));
    }
}

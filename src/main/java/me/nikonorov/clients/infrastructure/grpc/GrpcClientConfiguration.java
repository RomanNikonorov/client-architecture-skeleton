package me.nikonorov.clients.infrastructure.grpc;

import me.nikonorov.clients.infrastructure.grpc.generated.ExternalSystemAGrpc;
import me.nikonorov.clients.infrastructure.grpc.generated.ExternalSystemBGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

/**
 * Infrastructure configuration for generated outbound gRPC stubs.
 *
 * <p>Each bean is built from a named Spring gRPC channel. Channel addresses and
 * TLS settings are configured under {@code spring.grpc.client.channels.*}.</p>
 */
@Configuration
class GrpcClientConfiguration {

    /**
     * Creates the blocking stub used by the system A adapter.
     *
     * @param channels Spring gRPC channel factory
     * @param properties external-system configuration containing the channel name
     * @return generated blocking stub for system A
     */
    @Bean
    ExternalSystemAGrpc.ExternalSystemABlockingStub externalSystemAStub(
            GrpcChannelFactory channels,
            ExternalSystemsProperties properties
    ) {
        return ExternalSystemAGrpc.newBlockingStub(channels.createChannel(properties.systemA().channel()));
    }

    /**
     * Creates the blocking stub used by the system B adapter.
     *
     * @param channels Spring gRPC channel factory
     * @param properties external-system configuration containing the channel name
     * @return generated blocking stub for system B
     */
    @Bean
    ExternalSystemBGrpc.ExternalSystemBBlockingStub externalSystemBStub(
            GrpcChannelFactory channels,
            ExternalSystemsProperties properties
    ) {
        return ExternalSystemBGrpc.newBlockingStub(channels.createChannel(properties.systemB().channel()));
    }
}

package com.orderplatform.paymentservice.config;

import com.orderplatform.fraudcheckservice.grpc.FraudCheckServiceGrpc;
import io.grpc.Channel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class GrpcClientConfig {

    @Value("${fraud-check.grpc.address}")
    private String fraudCheckAddress;

    @Bean
    public FraudCheckServiceGrpc.FraudCheckServiceBlockingStub fraudCheckServiceBlockingStub(
            GrpcChannelFactory channelFactory) {
        Channel channel = channelFactory.createChannel(fraudCheckAddress);
        return FraudCheckServiceGrpc.newBlockingStub(channel);
    }
}
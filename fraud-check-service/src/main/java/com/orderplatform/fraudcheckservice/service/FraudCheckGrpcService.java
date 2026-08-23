package com.orderplatform.fraudcheckservice.service;

import com.orderplatform.fraudcheckservice.grpc.FraudCheckRequest;
import com.orderplatform.fraudcheckservice.grpc.FraudCheckResponse;
import com.orderplatform.fraudcheckservice.grpc.FraudCheckServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FraudCheckGrpcService extends FraudCheckServiceGrpc.FraudCheckServiceImplBase {

    @Override
    public void checkFraud(FraudCheckRequest request, StreamObserver<FraudCheckResponse> responseObserver) {

        log.info("Checking fraud for order {} (customer {}, amount {})",
                request.getOrderId(), request.getCustomerId(), request.getAmount());

        double riskScore = calculateRiskScore(request.getAmount());
        boolean approved = riskScore < 0.7;

        FraudCheckResponse response = FraudCheckResponse.newBuilder()
                .setOrderId(request.getOrderId())
                .setApproved(approved)
                .setRiskScore(riskScore)
                .setReason(approved ? "Risk within acceptable threshold" : "Risk score exceeds threshold")
                .build();

        log.info("Fraud check result for order {}: approved={}, riskScore={}",
                request.getOrderId(), approved, riskScore);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private double calculateRiskScore(double amount) {
        // Simplified mock scoring: larger orders carry proportionally more risk.
        // A real fraud service would call ML models, check velocity, device
        // fingerprints, geolocation, etc. This is a stand-in for that boundary.
        double score = (amount / 1000.0) * 0.6 + 0.05;
        return Math.min(score, 0.95);
    }
}
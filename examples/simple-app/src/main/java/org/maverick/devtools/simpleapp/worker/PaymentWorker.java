package org.maverick.devtools.simpleapp.worker;

import org.maverick.devtools.worker.annotation.ZeebeWorker;
import org.maverick.devtools.simpleapp.dto.PaymentInput;
import org.maverick.devtools.simpleapp.dto.PaymentOutput;
import io.camunda.zeebe.client.api.command.ClientException;
import org.maverick.devtools.worker.exception.ZeebeBpmnError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class PaymentWorker {

    @ZeebeWorker(type = "charge-card", fetchAllVariables = false, fetchVariables = { "cardNumber", "cvc", "expiryDate",
            "amount" })
    public PaymentOutput chargeCard(PaymentInput input) {
        log.info("Processing payment request: {}", input);

        try {
            validatePayment(input);

            // Simulate processing simulation
            String transactionId = UUID.randomUUID().toString();
            log.info("Payment successful. Transaction ID: {}", transactionId);

            return new PaymentOutput("SUCCESS", transactionId);
        } catch (IllegalArgumentException e) {
            // Throwing a ZeebeBpmnError allows the process to catch this error via an Error
            // Boundary Event
            log.warn("Business error occurred: {}", e.getMessage());
            throw new ZeebeBpmnError("PAYMENT_REJECTED", e.getMessage());
        } catch (Exception e) {
            // Uncaught runtime exceptions will trigger the job retries and eventually
            // create an Incident
            log.error("System error processing payment", e);
            throw e;
        }
    }

    private void validatePayment(PaymentInput input) {
        // Example business validation
        if (input.amount() != null && input.amount().signum() <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
        if ("EXPIRED".equals(input.expiryDate())) {
            throw new IllegalArgumentException("Card has expired");
        }
    }
}

package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.config.PayPalConfiguration;
import com.UmiUni.shop.config.StripeConfiguration;
import com.UmiUni.shop.entity.PayPalPayment;
import com.UmiUni.shop.entity.SalesOrder;
import com.UmiUni.shop.exception.PaymentProcessingException;
import com.UmiUni.shop.model.PayPalPaymentResponse;
import com.UmiUni.shop.model.PaymentResponse;
import com.UmiUni.shop.model.PaymentStatusResponse;
import com.UmiUni.shop.repository.PayPalPaymentRepository;
import com.UmiUni.shop.service.PayPalService;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Log4j2
public class PayPalServiceImpl implements PayPalService {

    // PayPal SDK setup and credentials
    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String clientSecret;

    @Value("${paypal.mode}")
    private String mode;

    @Autowired
    private PayPalPaymentRepository payPalPaymentRepository;

    @Autowired
    private TransactionTemplate transactionTemplate; // Inject the TransactionTemplate

    private APIContext getAPIContext() {
        return new APIContext(clientId, clientSecret, mode);

    }

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public PayPalPaymentResponse createPayment(SalesOrder salesOrder) {

        // Logic to create a payment
        // This typically involves setting up a payment amount, redirect URLs, and invoking the PayPal API to create a payment
        Amount amount = new Amount();
        amount.setCurrency("USD");
        amount.setTotal(salesOrder.getTotalAmount().toString());

        Transaction transaction = new Transaction();
        transaction.setDescription("Sales Order Transaction");
        transaction.setAmount(amount);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl("http://localhost:3000/paypal-return");  // cancel  paypal-return
        redirectUrls.setReturnUrl("http://localhost:3000/paypal-success");
        payment.setRedirectUrls(redirectUrls);

        try {

            // Condition 1: Simulate a condition that requires a rollback if the payment amount is negative
            if ( salesOrder.getTotalAmount().compareTo(BigDecimal.ZERO) < 0 ) {
                throw new PaymentProcessingException("Payment processing failed because the payment amount is negative.");
            }

            // Condition 2: Simulate a condition that requires a rollback if the customer has insufficient funds
            if (customerHasInsufficientFunds(salesOrder.getCustomerId(), salesOrder.getTotalAmount())) {
                throw new PaymentProcessingException("Payment processing failed due to insufficient funds.");
            }

            // Condition 3: Simulate a condition that requires payment to be interrupted
            if (customerExitsDuringPayment()) {
                throw new PaymentProcessingException("Payment creation interrupted because the customer exited.");
            }

            Payment createdPayment = payment.create(getAPIContext());
            log.info("createPayment: " + createdPayment);

            String payPalTransactionId = extractPaymentId(createdPayment);
            log.info("payPalTransactionId: " + payPalTransactionId);

            String payPalPaymentState = extractPaymentStatus(createdPayment);
            log.info("payPalPaymentStatus: " + payPalPaymentState);

            // Find the approval URL
            String approvalUrl = extractApprovalUrl(createdPayment);
            log.info("approvalUrl: " + approvalUrl);

            // Find the token
            String token = extractToken(approvalUrl);
            log.info("token: " + token);

            // Save the PayPalPaymentEntity
            PayPalPayment payPalPayment = PayPalPayment.builder()
                    .paypalToken(token)
                    .transactionId(payPalTransactionId)
                    .paymentState(payPalPaymentState)
                    .createTime(new Date())
                    .paymentMethod("PayPal")
                    .build();
            payPalPaymentRepository.save(payPalPayment);
            log.info("payPalPayment:" + payPalPayment);

            PayPalPaymentResponse response = new PayPalPaymentResponse("success create payment!", createdPayment.getId(), approvalUrl);
            log.info("create response: " + response);

            return response;

        } catch (PaymentProcessingException e) {
            e.printStackTrace();
            // Handle PayPal API exceptions
            log.error("Error creating payment: " + e.getMessage(), e);

            // If a transaction is active, mark it for rollback
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                // Mark the transaction for rollback
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }

            // The custom exception triggers a rollback
            return new PayPalPaymentResponse("Failed to create payment", null, null);
        } catch (PayPalRESTException e) {
            e.printStackTrace();
            // Handle PayPal API exceptions
            log.error("Error creating payment: " + e.getMessage(), e);

            // If a transaction is active, mark it for rollback
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                // Mark the transaction for rollback
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }

            return new PayPalPaymentResponse("Failed to create payment", null, null);
        } catch (Exception ex) {
            // Handle other unexpected exceptions
            log.error("Unexpected error creating payment: " + ex.getMessage(), ex);

            // If a transaction is active, mark it for rollback
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                // Mark the transaction for rollback
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }

            // can also rollback the payment or take other appropriate actions here
            return new PayPalPaymentResponse("Unexpected error", null, null);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public PaymentResponse completePayment(String paymentId, String payerId) {

        // Logic to execute a payment after the user approves it on PayPal's end
        // This typically involves using the PayPal API to execute the payment using the payment ID and payer ID
        Payment payment = new Payment();
        payment.setId(paymentId);

        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);

        try {

            // Condition 3: Simulate a condition that requires payment to be interrupted
            if (customerExitsDuringPayment()) {
                throw new PaymentProcessingException("Payment creation interrupted because the customer exited.");
            }

            // EXEC payment
            Payment executedPayment = payment.execute(getAPIContext(), paymentExecution);
            log.info("executedPayment: " + executedPayment);

            // update paypal payment entity status info
            PayPalPayment payPalPayment = payPalPaymentRepository.findByPaypalToken("EC-" + executedPayment.getCart());
            payPalPayment.setPaymentState("complete");
            payPalPayment.setUpdatedAt(executedPayment.getUpdateTime());
            payPalPaymentRepository.save(payPalPayment);
            log.info("update PayPalPayment: " + payPalPayment);

            PaymentResponse response = new PaymentResponse("success", executedPayment.getId());
            log.info("paymentId: " + paymentId + ", payerId: " + payerId, "complete response: " + response);

            return response;

        } catch (PaymentProcessingException e) {
            e.printStackTrace();
            // Handle PayPal API exceptions
            log.error("Error creating payment: " + e.getMessage(), e);

            // If a transaction is active, mark it for rollback
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                // Mark the transaction for rollback
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }

            // The custom exception triggers a rollback
            return new PaymentResponse("Failed to create payment", null);
        } catch (PayPalRESTException e) {
            e.printStackTrace();

            // If a transaction is active, mark it for rollback
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                // Mark the transaction for rollback
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }

            return new PaymentResponse("Failed to execute payment", null);
        } catch (Exception ex) {
            // Handle other unexpected exceptions
            log.error("Unexpected error executing payment: " + ex.getMessage(), ex);

            // If a transaction is active, mark it for rollback
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                // Mark the transaction for rollback
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }

            // You can also RollBack the payment or take other appropriate actions here
            return new PaymentResponse("Unexpected error", ex.getMessage());
        }
    }

    @Override
    public PaymentStatusResponse checkPaymentStatus(String token) {

        PayPalPayment payPalPayment = payPalPaymentRepository.findByPaypalToken(token);
        if (payPalPayment == null) {
            return new PaymentStatusResponse("Error", "Payment not found for token: " + token, 0);
        }

        String transactionId = payPalPayment.getTransactionId();
        PaymentStatusResponse response = new PaymentStatusResponse();

        try {

            APIContext apiContext = getAPIContext();

            // get payment through transactionId
            Payment payment = Payment.get(apiContext, transactionId);
            double amount = Double.parseDouble(payment.getTransactions().get(0).getAmount().getTotal());
            log.info("check the payment status: " + payment.getState());
            if (payment.getState().equals("created")) {
                log.info("*ERROR: Payment creation interrupted because the customer exited");
//                throw new PaymentProcessingException("Payment creation interrupted because the customer exited.");
                throw new RuntimeException("ERROR: Payment creation interrupted because the customer exited.");
            }
            log.info("PaymentStatusResponse: return null...");
            return new PaymentStatusResponse(payment.getState(), null, amount);

        } catch (PaymentProcessingException ex) {
            // Handle other unexpected exceptions
            log.error("Unexpected error creating payment: " + ex.getMessage(), ex);

            // If a transaction is active, mark it for rollback
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                // Mark the transaction for rollback
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }

            // can also rollback the payment or take other appropriate actions here
//            throw new PaymentProcessingException(ex.getMessage());
            return new PaymentStatusResponse("failed to process the payment", ex.getMessage(), 0);
        } catch (PayPalRESTException ex) {
            // Log the exception for debugging purposes
            // e.g., Logger.error("Error processing PayPal payment", e);
            response.setStatus("Error");
            response.setErrorDetails(ex.getMessage());
            // Log the exception
            return new PaymentStatusResponse("failed to process the payment", ex.getMessage(), 0);
//            return new PaymentStatusResponse("Error", e.getMessage(), 0);
        } catch (Exception ex) {
            // Handle other unexpected exceptions
            log.error("Unexpected error creating payment: " + ex.getMessage(), ex);

            // If a transaction is active, mark it for rollback
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                // Mark the transaction for rollback
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }

            // can also rollback the payment or take other appropriate actions here
            return new PaymentStatusResponse("failed to process the payment", ex.getMessage(), 0);
        }
    }

//    @Override
//    public String getPaymentStatus(String token) {
//        // Simulate retrieving the payment status based on the token
//        // In a real implementation, replace this with actual API calls to your payment gateway or PayPal
//
//        // Check if the token is empty or null
//        if (!StringUtils.hasText(token)) {
//            return "unknown"; // Unknown status when the token is empty or null
//        }
//
//        // Simulate checking the token prefix for different payment statuses
//        if (token.startsWith("success")) {
//            return "completed"; // Payment is completed
//        } else if (token.startsWith("created")) {
//            return "cancelled"; // Payment is cancelled
//        } else {
//            return "unknown"; // Unknown status for unrecognized token prefixes
//        }
//    }

    // Additional method to check if the customer has insufficient funds
    private boolean customerHasInsufficientFunds(Long customerId, BigDecimal amount) {
        // Logic to check if the customer's account balance is sufficient for the payment
        // Return true if the customer has insufficient funds, and false otherwise
        // You should implement this method based on your application's business rules
        // For demonstration purposes, we assume the customer has insufficient funds if the amount is greater than $1000
        return amount.compareTo(new BigDecimal("1000")) > 0;
    }

    // Additional method to simulate payment interruption during payment creation
    private boolean customerExitsDuringPayment() {
        // Logic to determine if payment creation should be interrupted
        // Return true if payment should be interrupted, and false otherwise
        // You can implement this method based on your application's needs
        // For demonstration purposes, we assume payment is interrupted if a certain condition is met
        return false;  // true false  Math.random() < 0.9
    }

//    // Additional method to simulate payment execution interruption
//    private boolean shouldInterruptPaymentExecution() {
//        // Logic to determine if payment execution should be interrupted
//        // Return true if payment execution should be interrupted, and false otherwise
//        // You can implement this method based on your application's needs
//        // For demonstration purposes, we assume payment execution is interrupted if a certain condition is met
//        return anotherConditionIsMet;
//    }

    private String extractPaymentId(Payment payment) {
        return payment.getId();
    }

    private String extractPaymentStatus(Payment payment) {
        return payment.getState();
    }

    private String extractApprovalUrl(Payment payment) {

        return payment.getLinks().stream()
                .filter(link -> "approval_url".equalsIgnoreCase(link.getRel()))  // approval_url  execute
                .findFirst()
                .map(link -> link.getHref())
                .orElse(null);
    }

    private String extractToken(String approvalUrl) {
        // Extract the token from the approval URL
        try {
            URI uri = new URI(approvalUrl);
            Map<String, List<String>> queryPairs = Stream.of(uri.getQuery().split("&"))
                    .map(param -> param.split("="))
                    .collect(Collectors.groupingBy(s -> s[0],
                            Collectors.mapping(s -> s.length > 1 ? s[1] : "", Collectors.toList())));

            // Assuming the token parameter is named 'token'
            List<String> tokens = queryPairs.get("token");
            return tokens != null && !tokens.isEmpty() ? tokens.get(0) : null;
        } catch (URISyntaxException e) {
            // Handle the exception as per your application's error handling policy
            // For example, log the error and/or return null
            log.error("Error parsing approval URL: ", e);
            return null;
        }
    }

}

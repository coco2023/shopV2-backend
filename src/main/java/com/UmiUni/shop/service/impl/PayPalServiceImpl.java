package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.constant.OrderStatus;
import com.UmiUni.shop.constant.PaymentMethod;
import com.UmiUni.shop.entity.PayPalPayment;
import com.UmiUni.shop.entity.SalesOrder;
import com.UmiUni.shop.exception.PaymentProcessingException;
import com.UmiUni.shop.model.PayPalPaymentResponse;
import com.UmiUni.shop.model.PaymentResponse;
import com.UmiUni.shop.model.PaymentStatusResponse;
import com.UmiUni.shop.repository.PayPalPaymentRepository;
import com.UmiUni.shop.repository.SalesOrderRepository;
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

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.*;
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

    private String frontendUrl = "https://www.quickmall24.com";

    @Autowired
    private PayPalPaymentRepository payPalPaymentRepository;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private TransactionTemplate transactionTemplate; // Inject the TransactionTemplate

    private APIContext getAPIContext() {
        return new APIContext(clientId, clientSecret, mode);

    }

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public PayPalPaymentResponse createPayment(SalesOrder salesOrder) {

        // get salesOrderSn
        String salesOrderSn = salesOrder.getSalesOrderSn();

        // Logic to create a payment
        Amount amount = new Amount();
        amount.setCurrency("USD");
        amount.setTotal(salesOrder.getTotalAmount().toString());

        Transaction transaction = new Transaction();
        //TODO: save SalesOrderSn
        transaction.setCustom(salesOrderSn);
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
        redirectUrls.setCancelUrl("https://www.quickmall24.com/paypal-return?salesOrderSn=`" + salesOrderSn);
        redirectUrls.setReturnUrl("https://www.quickmall24.com/paypal-success?salesOrderSn=" + salesOrderSn);
        payment.setRedirectUrls(redirectUrls);

        try {

            if ( salesOrder.getTotalAmount().compareTo(BigDecimal.ZERO) < 0 ) {
                throw new PaymentProcessingException("Payment processing failed because the payment amount is negative.");
            }

            if (customerHasInsufficientFunds(salesOrder.getCustomerId(), salesOrder.getTotalAmount())) {
                throw new PaymentProcessingException("Payment processing failed due to insufficient funds.");
            }

            if (customerExitsDuringPayment()) {
                throw new PaymentProcessingException("Payment creation interrupted because the customer exited.");
            }

            Payment createdPayment = payment.create(getAPIContext());

            String payPalTransactionId = extractPaymentId(createdPayment);

            String payPalPaymentState = extractPaymentStatus(createdPayment);

            // Find the approval URL
            String approvalUrl = extractApprovalUrl(createdPayment);

            // Find the token
            String token = extractToken(approvalUrl);

            // Save the PayPalPaymentEntity
            PayPalPayment payPalPayment = PayPalPayment.builder()
                    .paypalToken(token)
                    .transactionId(payPalTransactionId)
                    .paymentState(payPalPaymentState)
                    .createTime(new Date())
                    .paymentMethod("PayPal")
                    .build();
            payPalPaymentRepository.save(payPalPayment);
            log.info("createPayment: " + createdPayment,
                    "payPalTransactionId: " + payPalTransactionId,
                    "payPalPaymentStatus: " + payPalPaymentState,
                    "approvalUrl: " + approvalUrl,
                    "token: " + token,
                    "payPalPayment:" + payPalPayment);

            //TODO: update the orderStatus?
//            salesOrder.setOrderStatus(OrderStatus.PENDING);
            salesOrder.setLastUpdated(new Date().toString());

            //TODO: Lock the Product Inventory


            return new PayPalPaymentResponse("success create payment!", createdPayment.getId(), approvalUrl);

        } catch (PaymentProcessingException e) {
            e.printStackTrace();
            log.error("Error creating payment: " + e.getMessage(), e);

            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }

            return new PayPalPaymentResponse("Failed to create payment", null, null);
        } catch (PayPalRESTException e) {
            e.printStackTrace();
            log.error("Error creating payment: " + e.getMessage(), e);

            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }

            return new PayPalPaymentResponse("Failed to create payment", null, null);
        } catch (Exception ex) {
            log.error("Unexpected error creating payment: " + ex.getMessage(), ex);

            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }

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

            // update SalesOrder Info
            // get the salesOrderSn (customer)
            String salesOrderSn = extractCustomOrderSn(executedPayment);
            SalesOrder salesOrder = salesOrderRepository.getSalesOrderBySalesOrderSn(salesOrderSn).get();
            // Update the orderStatus
            salesOrder.setOrderStatus(OrderStatus.PROCESSING);
            salesOrder.setLastUpdated(executedPayment.getUpdateTime());
            log.info("update salesOrder status: " + salesOrder);
            salesOrderRepository.save(salesOrder);

            //TODO: Decrease the Product Inventory

            return new PaymentResponse("success", executedPayment.getId());

        } catch (PaymentProcessingException e) {
            e.printStackTrace();
            log.error("Error creating payment: " + e.getMessage(), e);

            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }

            return new PaymentResponse("Failed to create payment", null);
        } catch (PayPalRESTException e) {
            e.printStackTrace();

            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }

            return new PaymentResponse("Failed to execute payment", null);
        } catch (Exception ex) {
            log.error("Unexpected error executing payment: " + ex.getMessage(), ex);

            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }

            return new PaymentResponse("Unexpected error", ex.getMessage());
        }
    }

    @Override
    public PaymentStatusResponse checkPaymentStatus(String token) throws Exception {

        PayPalPayment payPalPayment = payPalPaymentRepository.findByPaypalToken(token);
        if (payPalPayment == null) {
            return new PaymentStatusResponse("Error", "Payment not found for token: " + token, 0, PaymentMethod.PAYPAL);
        }

        String transactionId = payPalPayment.getTransactionId();
        PaymentStatusResponse response = new PaymentStatusResponse();

        try {

            APIContext apiContext = getAPIContext();

            // get payment through transactionId
            Payment payment = Payment.get(apiContext, transactionId);
            double amount = Double.parseDouble(payment.getTransactions().get(0).getAmount().getTotal());
            log.info("check the payment status: " + payment.getState());
            if (!payment.getState().equals("complete")) {
                log.info("*ERROR: Payment creation interrupted because the customer exited");
//                throw new PaymentProcessingException("Payment creation interrupted because the customer exited.");
                throw new PaymentProcessingException("*ERROR: Payment creation interrupted because the customer exited.");
            }
            return new PaymentStatusResponse(payment.getState(), null, amount, PaymentMethod.PAYPAL);

        } catch (PaymentProcessingException ex) {
            log.error("Unexpected error creating payment: " + ex.getMessage(), ex);

            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }

            throw new PaymentProcessingException(ex.getMessage());
//            return new PaymentStatusResponse("failed to process the payment", ex.getMessage(), 0, PaymentMethod.PAYPAL);
        } catch (PayPalRESTException ex) {
            response.setStatus("Error");
            response.setErrorDetails(ex.getMessage());
            throw new PayPalRESTException(ex.getMessage());
//            return new PaymentStatusResponse("failed to process the payment", ex.getMessage(), 0, PaymentMethod.PAYPAL);
        } catch (Exception ex) {
            log.error("Unexpected error creating payment: " + ex.getMessage(), ex);

            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
            throw new Exception(ex.getMessage());
//            return new PaymentStatusResponse("failed to process the payment", ex.getMessage(), 0, PaymentMethod.PAYPAL);
        }
    }

    // Additional method to check if the customer has insufficient funds
    private boolean customerHasInsufficientFunds(Long customerId, BigDecimal amount) {
        // Logic to check if the customer's account balance is sufficient for the payment
        // Return true if the customer has insufficient funds, and false otherwise
        // For demonstration purposes, we assume the customer has insufficient funds if the amount is greater than $1000
        return amount.compareTo(new BigDecimal("1000")) > 0;
    }

    // Additional method to simulate payment interruption during payment creation
    private boolean customerExitsDuringPayment() {
        // Logic to determine if payment creation should be interrupted
        // Return true if payment should be interrupted, and false otherwise
        // For demonstration purposes, we assume payment is interrupted if a certain condition is met
        return false;  // true false  Math.random() < 0.9
    }

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

            List<String> tokens = queryPairs.get("token");
            return tokens != null && !tokens.isEmpty() ? tokens.get(0) : null;
        } catch (URISyntaxException e) {
            log.error("Error parsing approval URL: ", e);
            return null;
        }
    }

    private String extractCustomOrderSn(Payment payment) {
        return payment.getTransactions().stream()
                .filter(Objects::nonNull)
                .map(Transaction::getCustom)
                .findFirst()
                .orElse(null);
    }

}

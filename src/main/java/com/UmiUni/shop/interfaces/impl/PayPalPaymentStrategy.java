package com.UmiUni.shop.interfaces.impl;

import com.UmiUni.shop.constant.PaymentMethod;
import com.UmiUni.shop.constant.PaymentStatus;
import com.UmiUni.shop.entity.PayPalPayment;
import com.UmiUni.shop.entity.SalesOrder;
import com.UmiUni.shop.exception.PaymentProcessingException;
import com.UmiUni.shop.interfaces.APIContextFactory;
import com.UmiUni.shop.interfaces.PaymentStrategy;
import com.UmiUni.shop.model.PaymentResponse;
import com.UmiUni.shop.repository.PayPalPaymentRepository;
import com.UmiUni.shop.service.PaymentErrorHandlingService;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Log4j2
public class PayPalPaymentStrategy implements PaymentStrategy {

    @Value("${paypal.frontend.base.uri}") // paypal.frontend.base.test  // paypal.frontend.base.uri
    private String frontendUrl;           // "http://localhost:3000"   // https://www.quickmall24.com

    @Autowired
    private PayPalPaymentRepository payPalPaymentRepository;

    @Autowired
    private PaymentErrorHandlingService paymentErrorHandlingService;

    @Override
    // Implement create PayPal payment logic
    public PaymentResponse createPayPal(SalesOrder salesOrder, APIContext apiContext) throws PayPalRESTException {

        Payment createdPayment;
        PayPalPayment payPalPayment = null;
        try {
            validateOrder(salesOrder);

            Payment payment = setupPayment(salesOrder, apiContext);
            createdPayment = payment.create(apiContext);

            payPalPayment = saveResponseFromPayPalToDB(salesOrder, createdPayment);

            return new PaymentResponse(createdPayment.getState(), payPalPayment.getTransactionId(), payPalPayment.getApprovalURL());
        } catch (PaymentProcessingException e) {
            return  paymentErrorHandlingService.handlePaymentProcessingError(e, payPalPayment.getTransactionId(), salesOrder.getSalesOrderSn());
        } catch (PayPalRESTException e) {
            return paymentErrorHandlingService.handlePayPalRESTError(e, payPalPayment.getTransactionId(), salesOrder.getSalesOrderSn());
        } catch (Exception ex) {
            return paymentErrorHandlingService.handleGenericError(ex, payPalPayment.getTransactionId(), salesOrder.getSalesOrderSn());
        }
    }

    private PayPalPayment saveResponseFromPayPalToDB(SalesOrder salesOrder, Payment createdPayment) {
        // Extract necessary details from the created payment
        String payPalTransactionId = extractPaymentId(createdPayment);
        String payPalPaymentState = extractPaymentStatus(createdPayment);
        String approvalUrl = extractApprovalUrl(createdPayment); // Find the approval URL
        String token = extractToken(approvalUrl); // Find the token

        // Save the PayPalPaymentEntity
        PayPalPayment payPalPayment = PayPalPayment.builder()
                .paypalToken(token)
                .salesOrderSn(salesOrder.getSalesOrderSn())
                .transactionId(payPalTransactionId)
                .paymentState(payPalPaymentState)
                .createTime(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .paymentMethod("PayPal")
                .supplierId(String.valueOf(salesOrder.getSupplierId()))
                .approvalURL(approvalUrl)
                .status(PaymentStatus.CREATED.name())
                .paymentState(createdPayment.getState())
                .build();
        payPalPaymentRepository.save(payPalPayment);
        log.info("createPayment: " + createdPayment);
        log.info("payPalPayment: " + payPalPayment);

        return payPalPayment;
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

    private String extractApprovalUrl(Payment payment) {

        return payment.getLinks().stream()
                .filter(link -> "approval_url".equalsIgnoreCase(link.getRel()))  // approval_url  execute
                .findFirst()
                .map(link -> link.getHref())
                .orElse(null);
    }

    private String extractPaymentStatus(Payment payment) {
        return payment.getState();
    }

    private String extractPaymentId(Payment payment) {
        return payment.getId();
    }

    private Payment setupPayment(SalesOrder salesOrder, APIContext apiContext) {
        // get salesOrderSn
        String salesOrderSn = salesOrder.getSalesOrderSn();
        // get salesOrder expiredTime
        LocalDateTime expiredTime = salesOrder.getExpirationDate();
        // get supplierId
        Long supplierId = salesOrder.getSupplierId();

        // Logic to create a payment
        Amount amount = new Amount();
        amount.setCurrency("USD");
        amount.setTotal(salesOrder.getTotalAmount().toString());

        Transaction transaction = new Transaction();
        // Save SalesOrderSn
        transaction.setCustom(salesOrderSn);
        transaction.setAmount(amount);
        // convert expiredTime into string and save
        transaction.setDescription(expiredTime.toString());  // save expiredTime as description

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod(PaymentMethod.PAYPAL.name());

        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(frontendUrl + "/paypal-return?salesOrderSn=" + salesOrderSn + "&supplierId=" + supplierId);
        redirectUrls.setReturnUrl(frontendUrl + "/paypal-success?salesOrderSn=" + salesOrderSn + "&supplierId=" + supplierId);
        payment.setRedirectUrls(redirectUrls);
        log.info("redirectUrls: " + redirectUrls);

//        Payment createdPayment;
//        try {
//            createdPayment = payment.create(apiContext);
//        } catch (PayPalRESTException e) {
//            throw new RuntimeException(e);
//        }

        return payment;
    }

    private void validateOrder(SalesOrder salesOrder) throws PaymentProcessingException {
        if (salesOrder.getTotalAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new PaymentProcessingException("Payment processing failed because the payment amount is negative.");
        }

        if (customerHasInsufficientFunds(salesOrder.getCustomerId(), salesOrder.getTotalAmount())) {
            throw new PaymentProcessingException("Payment processing failed due to insufficient funds.");
        }

        if (customerExitsDuringPayment()) {
            throw new PaymentProcessingException("Payment creation interrupted because the customer exited.");
        }
    }

    // Additional method to simulate payment interruption during payment creation
    private boolean customerExitsDuringPayment() {
        // Logic to determine if payment creation should be interrupted
        // Return true if payment should be interrupted, and false otherwise
        // For demonstration purposes, we assume payment is interrupted if a certain condition is met
        return false;  // true false  Math.random() < 0.9
    }

    private boolean customerHasInsufficientFunds(Long customerId, BigDecimal amount) {
        // Logic to check if the customer's account balance is sufficient for the payment
        // Return true if the customer has insufficient funds, and false otherwise
        // For demonstration purposes, we assume the customer has insufficient funds if the amount is greater than $1000
        return amount.compareTo(new BigDecimal("1000")) > 0;
    }

}

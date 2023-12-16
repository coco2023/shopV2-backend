package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.constant.OrderStatus;
import com.UmiUni.shop.constant.PaymentMethod;
import com.UmiUni.shop.constant.PaymentStatus;
import com.UmiUni.shop.entity.PayPalPayment;
import com.UmiUni.shop.entity.PayPalPaymentResponseEntity;
import com.UmiUni.shop.entity.SalesOrder;
import com.UmiUni.shop.exception.PaymentProcessingException;
import com.UmiUni.shop.model.PayPalPaymentResponse;
import com.UmiUni.shop.model.PaymentResponse;
import com.UmiUni.shop.model.PaymentStatusResponse;
import com.UmiUni.shop.repository.PayPalPaymentRepository;
import com.UmiUni.shop.repository.PayPalPaymentResponseRepo;
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

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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

    private String frontendUrl = "http://localhost:3000"; // "http://localhost:3000" https://www.quickmall24.com

    @Autowired
    private PayPalPaymentRepository payPalPaymentRepository;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private PayPalPaymentResponseRepo payPalPaymentResponseRepo;

    private APIContext getAPIContext() {
        return new APIContext(clientId, clientSecret, mode);

    }

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public PayPalPaymentResponse createPayment(SalesOrder salesOrderRequest) {

        log.info("start create payment");
        SalesOrder salesOrder = salesOrderRepository.getSalesOrderBySalesOrderSn(salesOrderRequest.getSalesOrderSn()).get();
        log.info(salesOrder.getExpirationDate());

        // get salesOrderSn
        String salesOrderSn = salesOrder.getSalesOrderSn();
        // get salesOrder expiredTime
        LocalDateTime expiredTime = salesOrder.getExpirationDate();

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
        payer.setPaymentMethod("paypal");

        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(transactions);


        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(frontendUrl + "/paypal-return?salesOrderSn=" + salesOrderSn);
        redirectUrls.setReturnUrl(frontendUrl + "/paypal-success?salesOrderSn=" + salesOrderSn);
        payment.setRedirectUrls(redirectUrls);
        log.info("redirectUrls: " + redirectUrls);

        try {

            if ( salesOrder.getTotalAmount().compareTo(BigDecimal.ZERO) < 0 ) {
                // TODO: save the error logs during PayPal payment action
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
                    .createTime(new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                    .paymentMethod("PayPal")
                    .build();
            payPalPaymentRepository.save(payPalPayment);
            log.info("createPayment: " + createdPayment);
            log.info("payPalPayment: " + payPalPayment);

            // update the orderStatus
            salesOrder.setOrderStatus(OrderStatus.PENDING);
            salesOrder.setLastUpdated(
                    new Date()
                            .toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime()
            );

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

        APIContext apiContext = getAPIContext();

        try {

            if (customerExitsDuringPayment()) {
                throw new PaymentProcessingException("Payment creation interrupted because the customer exited.");
            }

            // check if payment has been expired
            // Retrieve the payment object from PayPal
            Payment getCreatePayment = Payment.get(apiContext, paymentId);
//            log.info("Retrieved payment: " + getCreatePayment);

            // get cart(Token)
            PayPalPayment payPalPayment = payPalPaymentRepository.findByPaypalToken("EC-" + getCreatePayment.getCart());
            log.info("**payPalPayment: " + payPalPayment);

            // get expireDate
            String expiredDate = extractDescription(getCreatePayment);
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime expiredTime = LocalDateTime.parse(expiredDate, formatter);
            log.info("this is expiredDate: " + expiredDate);

            if (expiredTime.isBefore(LocalDateTime.now())) {
                // update SalesOrder Status to EXPIRED
                // get salesOrderSn
                String salesOrderSn = extractCustomOrderSn(getCreatePayment);
                SalesOrder salesOrder = salesOrderRepository.getSalesOrderBySalesOrderSn(salesOrderSn).get();
                salesOrder.setOrderStatus(OrderStatus.EXPIRED);
                salesOrder.setLastUpdated(LocalDateTime.now());
                log.info("update salesOrder: " + salesOrder);
                salesOrderRepository.save(salesOrder);

                // update PayPalPayment info
                payPalPayment.setPaymentState(PaymentStatus.EXPIRED.name());
                payPalPayment.setUpdatedAt(LocalDateTime.now());
                log.info("update payPalPayment: " + payPalPayment);
                payPalPaymentRepository.save(payPalPayment);

//                throw new PaymentProcessingException("*ERROR: Payment has been expired!!");
                return new PaymentResponse("expired", getCreatePayment.getId());
            }

            // Logic to execute a payment after the user approves it on PayPal's end
            // This typically involves using the PayPal API to execute the payment using the payment ID and payer ID
            Payment payment = new Payment();
            payment.setId(paymentId);

            PaymentExecution paymentExecution = new PaymentExecution();
            paymentExecution.setPayerId(payerId);

            // EXEC payment
            Payment executedPayment = payment.execute(apiContext, paymentExecution);
            log.info("executedPayment: " + executedPayment);

            // save the executedPayment response
            saveExecutedPayment(executedPayment);

            // update paypal payment entity status info
            payPalPayment.setPaymentState("complete");
            payPalPayment.setUpdatedAt(
                    ZonedDateTime.parse(executedPayment.getUpdateTime(), DateTimeFormatter.ISO_DATE_TIME)
                            .toLocalDateTime()
            );
            payPalPaymentRepository.save(payPalPayment);
            log.info("update PayPalPayment: " + payPalPayment);

            // update SalesOrder Info
            // get the salesOrderSn (customer)
            String salesOrderSn = extractCustomOrderSn(executedPayment);
            SalesOrder salesOrder = salesOrderRepository.getSalesOrderBySalesOrderSn(salesOrderSn).get();
            // Update the orderStatus
            salesOrder.setOrderStatus(OrderStatus.PROCESSING);
            salesOrder.setLastUpdated(
                    LocalDateTime.parse(
                            executedPayment.getUpdateTime(),
                            DateTimeFormatter.ISO_DATE_TIME)
            );

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

            // check if salesOrder is expired
            String expiredTime = extractDescription(payment);
            // convert string into Date time & LocalTime
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime time = LocalDateTime.parse(expiredTime, formatter);

            // check if the payment has expired
            if (time.isBefore(LocalDateTime.now())) {
                // update SalesOrder Status to EXPIRED
                // get salesOrderSn
                String salesOrderSn = extractCustomOrderSn(payment);
                SalesOrder salesOrder = salesOrderRepository.getSalesOrderBySalesOrderSn(salesOrderSn).get();
                payment.setState(OrderStatus.EXPIRED.name());
                payPalPayment.setPaymentState(PaymentStatus.EXPIRED.name());
                salesOrder.setOrderStatus(OrderStatus.EXPIRED);
                payPalPaymentRepository.save(payPalPayment);
                salesOrderRepository.save(salesOrder);

//                throw new PaymentProcessingException("*ERROR: Payment has been expired!");
                return new PaymentStatusResponse(payment.getState(), "*ERROR: Payment has been expired!", 0, PaymentMethod.PAYPAL);
            }

            double amount = Double.parseDouble(payment.getTransactions().get(0).getAmount().getTotal());
            log.info("check the payment status: " + payment.getState());
            if (!payment.getState().equals("complete")) {
                log.info("*ERROR: Payment creation interrupted because the customer exited");
                // payment rollback, throw the error
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

    @Override
    public List<PayPalPaymentResponseEntity> getAllPayPalPaymentResponseEntity() {
        List<PayPalPaymentResponseEntity> payPalPaymentResponseEntity = payPalPaymentResponseRepo.findAll();
        log.info("payPalPaymentResponseEntity: " + payPalPaymentResponseEntity);
        return payPalPaymentResponseEntity;
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

    private String extractDescription(Payment payment) {
        return payment.getTransactions().stream()
                .filter(Objects::nonNull)
                .map(Transaction::getDescription)
                .findFirst()
                .orElse(null);
    }

    private PayPalPaymentResponseEntity saveExecutedPayment(Payment executedPayment) {
        try{
            PayPalPaymentResponseEntity paymentEntity = new PayPalPaymentResponseEntity();

            // Basic payment info
            paymentEntity.setPaymentId(executedPayment.getId());
            paymentEntity.setIntent(executedPayment.getIntent());
            paymentEntity.setCart(executedPayment.getCart());
            paymentEntity.setState(executedPayment.getState());
            paymentEntity.setCreateTime(executedPayment.getCreateTime());
            paymentEntity.setUpdateTime(executedPayment.getUpdateTime());

            // Payer info
            Payer payer = executedPayment.getPayer();
            PayerInfo payerInfo = payer.getPayerInfo();
            paymentEntity.setPayerStatus(payer.getStatus());
            paymentEntity.setPayerEmail(payerInfo.getEmail());
            paymentEntity.setPayerFirstName(payerInfo.getFirstName());
            paymentEntity.setPayerLastName(payerInfo.getLastName());
            paymentEntity.setPayerId(payerInfo.getPayerId());
            paymentEntity.setPayerCountryCode(payerInfo.getCountryCode());

            // Shipping address
            if (payerInfo.getShippingAddress() != null) {
                paymentEntity.setShippingRecipientName(payerInfo.getShippingAddress().getRecipientName());
                paymentEntity.setShippingLine1(payerInfo.getShippingAddress().getLine1());
                paymentEntity.setShippingCity(payerInfo.getShippingAddress().getCity());
                paymentEntity.setShippingState(payerInfo.getShippingAddress().getState());
                paymentEntity.setShippingCountryCode(payerInfo.getShippingAddress().getCountryCode());
                paymentEntity.setShippingPostalCode(payerInfo.getShippingAddress().getPostalCode());
            }

            // Assuming only one transaction for simplicity
            Transaction transaction = executedPayment.getTransactions().get(0);
            log.info("transaction: " + transaction + "; custom: " + transaction.getCustom() + "; " + transaction.getDescription());
            Amount transactionAmount = transaction.getAmount();
            paymentEntity.setTransactionAmountCurrency(transactionAmount.getCurrency());
            paymentEntity.setTransactionAmountTotal(Double.parseDouble(transactionAmount.getTotal()));
            paymentEntity.setTransactionDescription(transaction.getDescription());
            paymentEntity.setTransactionCustom(transaction.getCustom());
            paymentEntity.setTransactionSoftDescriptor(transaction.getSoftDescriptor());

            // Payee info
            Payee payee = transaction.getPayee();
            paymentEntity.setPayeeEmail(payee.getEmail());
            paymentEntity.setPayeeMerchantId(payee.getMerchantId());

            // Sale info
            List<RelatedResources> relatedResources = transaction.getRelatedResources();
            if (!relatedResources.isEmpty()) {
                Sale sale = relatedResources.get(0).getSale();
                paymentEntity.setSaleId(sale.getId());
                paymentEntity.setSaleState(sale.getState());
                paymentEntity.setSalePaymentMode(sale.getPaymentMode());
                paymentEntity.setSaleProtectionEligibility(sale.getProtectionEligibility());
                paymentEntity.setSaleProtectionEligibilityType(sale.getProtectionEligibilityType());
                paymentEntity.setSaleCreateTime(sale.getCreateTime());
                paymentEntity.setSaleUpdateTime(sale.getUpdateTime());

                Amount saleAmount = sale.getAmount();
                paymentEntity.setSaleAmountCurrency(saleAmount.getCurrency());
                paymentEntity.setSaleAmountTotal(Double.parseDouble(saleAmount.getTotal()));

                Details details = saleAmount.getDetails();
                paymentEntity.setSaleAmountDetailsSubtotal(details.getSubtotal());
                paymentEntity.setSaleAmountDetailsShipping(details.getShipping());
                paymentEntity.setSaleAmountDetailsHandlingFee(details.getHandlingFee());
                paymentEntity.setSaleAmountDetailsShippingDiscount(details.getShippingDiscount());
                paymentEntity.setSaleAmountDetailsInsurance(details.getInsurance());

                if (sale.getTransactionFee() != null) {
                    paymentEntity.setSaleTransactionFeeCurrency(sale.getTransactionFee().getCurrency());
                    paymentEntity.setSaleTransactionFeeValue(Double.parseDouble(sale.getTransactionFee().getValue()));
                }
            }

            log.info("successfully save the executedPayment!");

            // Save to repository
            return payPalPaymentResponseRepo.save(paymentEntity);

        } catch (Exception e) {
            // Handle exceptions
            e.printStackTrace();
            return null;
        }
    }

}

package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.config.PayPalConfiguration;
import com.UmiUni.shop.config.StripeConfiguration;
import com.UmiUni.shop.entity.SalesOrder;
import com.UmiUni.shop.model.PayPalPaymentResponse;
import com.UmiUni.shop.model.PaymentResponse;
import com.UmiUni.shop.service.PayPalService;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

//    private final PayPalConfiguration payPalConfiguration;
//
//    @Autowired
//    public PayPalServiceImpl(PayPalConfiguration payPalConfiguration) {
//        this.payPalConfiguration = payPalConfiguration;
//    }

//    public void makePayment() {
//        String paypalClientId = payPalConfiguration.getClientId();
//        String paypalClientSecret = payPalConfiguration.getSecret();
//
//        // Use the Stripe and PayPal credentials in your payment processing logic
//    }

    private APIContext getAPIContext() {
//        String clientId = payPalConfiguration.getClientId();
//        String clientSecret = payPalConfiguration.getSecret();

        return new APIContext(clientId, clientSecret, mode);

    }

    @Override
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
        redirectUrls.setCancelUrl("http://localhost:3000/cancel");
        redirectUrls.setReturnUrl("http://localhost:3000/paypal-success");
        payment.setRedirectUrls(redirectUrls);

        try {
            Payment createdPayment = payment.create(getAPIContext());
            log.info("createPayment: " + createdPayment);
            // Find the approval URL
            String approvalUrl = createdPayment.getLinks().stream()
                    .filter(link -> "approval_url".equalsIgnoreCase(link.getRel()))  // approval_url  execute
                    .findFirst()
                    .map(link -> link.getHref())
                    .orElse(null);
            log.info("approval_url: " + approvalUrl);  // approval_url   execute
            PayPalPaymentResponse response = new PayPalPaymentResponse("success create payment!", createdPayment.getId(), approvalUrl);
            log.info("create response: " + response);
            return response;
        } catch (PayPalRESTException e) {
            e.printStackTrace();
            return new PayPalPaymentResponse("failed create payment", null, null);
        }
    }

    @Override
    public PaymentResponse completePayment(String paymentId, String payerId) {
        // Logic to execute a payment after the user approves it on PayPal's end
        // This typically involves using the PayPal API to execute the payment using the payment ID and payer ID
        Payment payment = new Payment();
        payment.setId(paymentId);

        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);

        try {
            Payment executedPayment = payment.execute(getAPIContext(), paymentExecution);
            log.info("executedPayment: " + executedPayment);
            PaymentResponse response = new PaymentResponse("success", executedPayment.getId());
            log.info("paymentId: " + paymentId + ", payerId: " + payerId, "complete response: " + response);
            return response;
        } catch (PayPalRESTException e) {
            e.printStackTrace();
            return new PaymentResponse("failed complete payment", null);
        }
    }
}

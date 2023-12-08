package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.config.PayPalConfiguration;
import com.UmiUni.shop.config.StripeConfiguration;
import com.UmiUni.shop.constant.PaymentStatus;
import com.UmiUni.shop.entity.Payment;
import com.UmiUni.shop.entity.SalesOrder;
import com.UmiUni.shop.model.PaymentResponse;
import com.UmiUni.shop.repository.PaymentRepository;
import com.UmiUni.shop.service.StripeService;
import com.stripe.Stripe;
import com.stripe.model.Charge;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Log4j2
public class StripeServiceImpl implements StripeService {

    @Value("${stripe.secret.key}")
    private String apiKey;

//    private final StripeConfiguration stripeConfiguration;
//
//    @Autowired
//    public StripeServiceImpl(StripeConfiguration stripeConfiguration) {
//        this.stripeConfiguration = stripeConfiguration;
//    }

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public PaymentResponse createCharge(SalesOrder salesOrder, String token) {

        try{

//            Stripe.apiKey = stripeConfiguration.getSecretKey();
//            log.info("Stripe seceretKey: " + stripeConfiguration.getSecretKey());
            Stripe.apiKey = apiKey;

            Map<String, Object> chargeParams = new HashMap<>();
            chargeParams.put("amount", salesOrder.getTotalAmount().multiply(new BigDecimal(100)).intValue()); // Convert to cents
            chargeParams.put("currency", "usd"); // Assuming USD, modify as necessary
            chargeParams.put("source", token); // Token obtained from Stripe.js on the frontend
            chargeParams.put("description", "Charge for order " + salesOrder.getSalesOrderSn());
            chargeParams.put("receipt_email", salesOrder.getCustomerEmail()); // Customer's email
//            chargeParams.put("customer", salesOrder.getCustomerName());

            // Optional: Add metadata for tracking
            Map<String, Object> metadata = new HashMap<>();
//            metadata.put("sales_order_id", salesOrder.getSalesOrderId().toString());
            metadata.put("sales_order", salesOrder);
            metadata.put("customer_id", salesOrder.getCustomerId().toString());
            metadata.put("shipping_address", salesOrder.getShippingAddress());
            metadata.put("billing_address", salesOrder.getBillingAddress());
            chargeParams.put("metadata", metadata);

            Charge charge = Charge.create(chargeParams);

            // Save or update payment details in the database
            Payment payment = new Payment();
//            payment.setInvoiceId(salesOrder.getSalesOrderId()); // Assuming salesOrderId is used as invoiceId
            payment.setTransactionId(charge.getId());
//            payment.setSalesOrderId(salesOrder.getSalesOrderId());
            payment.setSalesOrderSn(salesOrder.getSalesOrderSn());
            payment.setCurrency("USD");
            payment.setPaymentDate(LocalDateTime.now());
            payment.setCustomerEmail(salesOrder.getCustomerEmail());
            payment.setPaymentStatus(mapStripeStatusToPaymentStatus(charge.getStatus()));
            payment.setPaymentMethod(salesOrder.getPaymentMethod()); // Since Stripe is used here
            payment.setPaymentMethodDetails(charge.getPaymentMethodDetails().getType());
            payment.setAmount(salesOrder.getTotalAmount());
            payment.setReceiptUrl(charge.getReceiptUrl()); // Set the receipt URL

            payment.setErrorMessage(charge.getFailureMessage()); // In case of failure
            paymentRepository.save(payment);

            return new PaymentResponse(charge.getStatus(), charge.getId());

        } catch (Exception e) {
            e.printStackTrace();
            return new PaymentResponse("failed", e.getMessage());
        }
    }

    private PaymentStatus mapStripeStatusToPaymentStatus(String stripeStatus) {
        // Map Stripe's charge status to your PaymentStatus enum
        switch (stripeStatus) {
            case "succeeded":
                return PaymentStatus.SUCCESS;
            case "failed":
                return PaymentStatus.FAILED;
            default:
                return PaymentStatus.PENDING;
        }
    }

}

package com.UmiUni.shop.controller;

import com.UmiUni.shop.entity.Payment;
import com.UmiUni.shop.entity.SalesOrder;
import com.UmiUni.shop.model.PayPalPaymentResponse;
import com.UmiUni.shop.model.PaymentResponse;
import com.UmiUni.shop.model.StripePaymentRequest;
import com.UmiUni.shop.service.PayPalService;
import com.UmiUni.shop.service.PaymentService;
import com.UmiUni.shop.service.StripeService;
import io.swagger.annotations.Api;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@Api(value = "PaymentController")
@Log4j2
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private StripeService stripeService;

    @Autowired
    private PayPalService payPalService;

    @PostMapping
    public ResponseEntity<Payment> createPayment(@RequestBody Payment payment) {
        return ResponseEntity.ok(paymentService.createPayment(payment));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPayment(id));
    }

    @GetMapping("/all")
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Payment> updatePayment(@PathVariable Long id, @RequestBody Payment paymentDetails) {
        return ResponseEntity.ok(paymentService.updatePayment(id, paymentDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Stripe Payment
     */
    @PostMapping("/stripe/charge")
    public ResponseEntity<?> createCharge(@RequestBody StripePaymentRequest request) {
        try {
            PaymentResponse response = stripeService.createCharge(request.getSalesOrder(), request.getToken());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Payment failed: " + e.getMessage());
        }
    }

    /**
     * PayPal
     */
    // Endpoint to create a payment
    @PostMapping("/paypal/create")
    public ResponseEntity<?> createPayment(@RequestBody SalesOrder salesOrder) {
        try {
            PayPalPaymentResponse paymentResponse = payPalService.createPayment(salesOrder);
            return ResponseEntity.ok(paymentResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating PayPal payment: " + e.getMessage());
        }
    }

    // Endpoint to complete a payment
    @PostMapping("/paypal/complete")
    public ResponseEntity<?> completePayment(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId) {
        try {
            PaymentResponse paymentResponse = payPalService.completePayment(paymentId, payerId);
            return ResponseEntity.ok(paymentResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error completing PayPal payment: " + e.getMessage());
        }
    }

}

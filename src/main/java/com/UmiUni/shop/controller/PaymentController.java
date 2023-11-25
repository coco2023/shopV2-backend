package com.UmiUni.shop.controller;

import com.UmiUni.shop.entity.Payment;
import com.UmiUni.shop.model.PaymentResponse;
import com.UmiUni.shop.model.StripePaymentRequest;
import com.UmiUni.shop.service.PaymentService;
import com.UmiUni.shop.service.StripeService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@Api(value = "PaymentController")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private StripeService stripeService;


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
}

package com.UmiUni.shop.controller;

import com.UmiUni.shop.dto.PayPalPaymentResponseDTO;
import com.UmiUni.shop.entity.Payment;
import com.UmiUni.shop.entity.PaymentErrorLog;
import com.UmiUni.shop.entity.SalesOrder;
import com.UmiUni.shop.exception.PaymentProcessingException;
import com.UmiUni.shop.model.*;
import com.UmiUni.shop.service.PayPalService;
import com.UmiUni.shop.service.PaymentService;
import com.UmiUni.shop.service.StripeService;
import com.paypal.base.rest.PayPalRESTException;
import io.swagger.annotations.Api;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    // get all EXEC paypal records (get PayPalPaymentResponseEntity)
    @GetMapping("/paypal/execute-payment-record/all")
    // need to use DTO data model coz the json is too long
    public ResponseEntity<List<PayPalPaymentResponseDTO>> getAllPayPalPaymentResponseEntity() {
        List<PayPalPaymentResponseDTO> payPalPaymentResponseEntities = payPalService.getAllPayPalPaymentResponseEntity();
        return ResponseEntity.ok(payPalPaymentResponseEntities);
    }

    // Endpoint to create a payment
    @PostMapping("/paypal/create")
    public ResponseEntity<?> createPayment(@RequestBody SalesOrder salesOrder) {
        try {
            PaymentResponse paymentResponse = payPalService.createPayment(salesOrder);
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

    @GetMapping("/paypal/check-payment-status")
    public ResponseEntity<?> checkPaymentStatus(@RequestParam("token") String token) throws Exception {
        try {
            PaymentResponse response = payPalService.checkPaymentStatus(token);
            return ResponseEntity.ok(response);
        } catch (PaymentProcessingException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            throw new PaymentProcessingException(e.getMessage());
        } catch (PayPalRESTException ex) {
            throw new PayPalRESTException(ex.getMessage());
        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            throw new Exception(e.getMessage());
        }
    }

    /**
     * get payment error logs and deliver to frontend page
     */
    @GetMapping("/get-payment-error-logs/all")
    public ResponseEntity<List<PaymentErrorLog>> getPaymentErrorLog() {
        List<PaymentErrorLog> paymentErrorLogs = payPalService.getPaymentErrorLog();
//        log.info("paymentErrorLogs: " + paymentErrorLogs);
        return ResponseEntity.ok(paymentErrorLogs);
    }

    /**
     * notification for customer exit the payment
     */
    @PostMapping("/notify-exit")
    public ResponseEntity<String> notifyExit(@RequestBody ExitNotification exitNotification) {
        // Log the exit event with a timestamp
        String exitReason = exitNotification.getExitReason();
        LocalDateTime exitTime = LocalDateTime.now();
        log.info("User exited the payment process. Reason: {}. Timestamp: {}", exitReason, exitTime);

//      // Throw a PaymentProcessingException to simulate interruption due to exit
//        return ResponseEntity.ok("Exit notification received.");
        throw new PaymentProcessingException("Payment creation interrupted because the customer exited.");
    }

    @PostMapping("/paypal-webhook")
    public ResponseEntity<Void> handlePayPalWebhook(@RequestBody String payload) {
        // Parse and process the PayPal webhook payload
        // Implement your logic here to handle payment events
        // You can log, update your database, or send notifications

        // Return a 200 OK response to PayPal to acknowledge receipt of the webhook
        return ResponseEntity.status(HttpStatus.OK).build();
    }


}

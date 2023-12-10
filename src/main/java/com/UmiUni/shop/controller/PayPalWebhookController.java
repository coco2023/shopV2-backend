//package com.UmiUni.shop.controller;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//public class PayPalWebhookController {
//
//    @PostMapping("/paypal-webhook")
//    public ResponseEntity<Void> handlePayPalWebhook(@RequestBody String payload) {
//        // Parse and process the PayPal webhook payload
//        // Implement your logic here to handle payment events
//        // You can log, update your database, or send notifications
//
//        // Return a 200 OK response to PayPal to acknowledge receipt of the webhook
//        return ResponseEntity.status(HttpStatus.OK).build();
//    }
//}

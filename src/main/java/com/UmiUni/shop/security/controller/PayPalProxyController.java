//package com.UmiUni.shop.security.controller;
//
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.http.*;
//import org.springframework.web.util.UriComponentsBuilder;
//
//@RestController
//@RequestMapping("/api/proxy")
//public class PayPalProxyController {
//
//    private final RestTemplate restTemplate;
//
//    public PayPalProxyController() {
//        this.restTemplate = new RestTemplate();
//    }
//
//    @GetMapping("/paypal")
//    public ResponseEntity<String> proxyToPayPal(@RequestParam String responseType,
//                                                @RequestParam String clientId,
//                                                @RequestParam String state,
//                                                @RequestParam String redirectUri) {
//        String url = "https://www.sandbox.paypal.com/signin/authorize";
//
//        // Add all required parameters to the URL
//        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
//                .queryParam("response_type", responseType)
//                .queryParam("client_id", clientId)
//                .queryParam("state", state)
//                .queryParam("redirect_uri", redirectUri);
//
//        // Make the request to PayPal
//        ResponseEntity<String> paypalResponse = restTemplate.getForEntity(builder.toUriString(), String.class);
//
//        // Return the response from PayPal, including the appropriate CORS headers
//        HttpHeaders responseHeaders = new HttpHeaders();
//        responseHeaders.setAccessControlAllowOrigin("*"); // Set this to your frontend's origin
//        return new ResponseEntity<>(paypalResponse.getBody(), responseHeaders, paypalResponse.getStatusCode());
//    }
//}

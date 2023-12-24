package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.entity.Supplier;
import com.UmiUni.shop.repository.SupplierRepository;
import com.UmiUni.shop.service.SupplierService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.net.URLEncoder;

import org.springframework.http.*;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
public class SupplierServiceImpl implements SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    public Supplier createSupplier(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    public Supplier getSupplier(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + id));
    }

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public Supplier updateSupplier(Long id, Supplier supplierDetails) {
        Supplier supplier = getSupplier(id);
        supplier.setSupplierName(supplierDetails.getSupplierName());
        supplier.setContactInfo(supplierDetails.getContactInfo());
        supplier.setPaypalEmail(supplier.getPaypalEmail());
//        supplier.setPaypalAccessToken(supplier.getPaypalAccessToken());
        // other updates as needed
        return supplierRepository.save(supplier);
    }

    public void deleteSupplier(Long id) {
        supplierRepository.deleteById(id);
    }

    @Override
    public String initiatePaypalAuthorization(Long supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId).orElse(null);
        if (supplier == null) {
            throw new IllegalArgumentException("Supplier not found");
        }

        String baseRedirectUri = "https://aee2-66-253-183-231.ngrok-free.app/api/v1/suppliers/v2/callback";
//        String baseRedirectUri = "localhost:9001/api/v1/suppliers/v2/callback";

        String redirectUri = baseRedirectUri; // + supplierId;
        String encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        log.info("redirectUri: " + redirectUri);

        supplier.setPaypalRedirectUri(redirectUri);
        supplierRepository.save(supplier);

        String state = encryptOrEncodeSupplierId(supplierId); // Your method to encrypt or encode the supplier ID
        String authUrl = "https://www.sandbox.paypal.com/signin/authorize?client_id="
                + supplier.getPaypalClientId()
                + "&response_type=code"
//                + "&scope=openid%20email%20profile"
//                + "scope=openid%20https://uri.paypal.com/services/payments/realtimepayment"
//                + "scope=https://uri.paypal.com/services/invoicing https://uri.paypal.com/services/disputes/read-buyer https://uri.paypal.com/services/payments/realtimepayment https://uri.paypal.com/services/disputes/update-seller https://uri.paypal.com/services/payments/payment/authcapture openid https://uri.paypal.com/services/disputes/read-seller https://uri.paypal.com/services/payments/refund https://api-m.paypal.com/v1/vault/credit-card https://api-m.paypal.com/v1/payments/.* https://uri.paypal.com/payments/payouts https://api-m.paypal.com/v1/vault/credit-card/.* https://uri.paypal.com/services/subscriptions https://uri.paypal.com/services/applications/webhooks"
                + "&redirect_uri=" + encodedRedirectUri
                + "&state=" + state;

        log.info("authUrl: " + authUrl);
        return authUrl;
    }

    @Override
    public String completePaypalAuthorization(
            String authorizationCode, String state) {

        Long supplierId = decryptOrDecodeSupplierId(state);
        Supplier supplier = supplierRepository.findById(supplierId).orElse(null);
        if (supplier == null) {
            throw new IllegalArgumentException("Supplier not found");
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(supplier.getPaypalClientId(), supplier.getPaypalClientSecret());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        String requestBody = null;
        try {
            requestBody = "grant_type=authorization_code&code=" + authorizationCode
                    + "&redirect_uri=" + URLEncoder.encode(supplier.getPaypalRedirectUri(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "https://api-m.sandbox.paypal.com/v1/oauth2/token",
                HttpMethod.POST, request, String.class);
        log.info("PayPal OAuth token exchange request: " + request + "; response: " + response);

        if (response.getStatusCode() == HttpStatus.OK) {
            // Extract and return the access token from the response
            return extractAccessToken(response.getBody());
        } else {
            throw new RuntimeException("Failed to exchange authorization code");
        }
    }

    private String extractAccessToken(String responseBody) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, String> responseMap = objectMapper.readValue(responseBody, Map.class);
            return responseMap.get("access_token");
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse access token", e);
        }
    }

    public String encryptOrEncodeSupplierId(Long supplierId) {
        return Base64.getUrlEncoder().encodeToString(supplierId.toString().getBytes());
    }

    public Long decryptOrDecodeSupplierId(String encodedId) {
        return Long.parseLong(new String(Base64.getUrlDecoder().decode(encodedId)));
    }


}

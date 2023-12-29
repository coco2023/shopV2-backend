package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.dto.PaypalConfigurationDto;
import com.UmiUni.shop.entity.Supplier;
import com.UmiUni.shop.entity.SupplierPayPalAuth;
import com.UmiUni.shop.model.PayPalInfo;
import com.UmiUni.shop.repository.SupplierPayPalAuthRepo;
import com.UmiUni.shop.repository.SupplierRepository;
import com.UmiUni.shop.service.SupplierService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.net.URLEncoder;

import org.springframework.http.*;

@Service
@Log4j2
public class SupplierServiceImpl implements SupplierService {

    @Value("${paypal.redirect.test}") // paypal.redirect.uri  // paypal.redirect.test
    private String baseRedirectUri;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private SupplierPayPalAuthRepo supplierPayPalRootRepo;

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

    /**
     * current Receiving money (merchant) account does not connect to the PayPal Oauth login account.
     * @param supplierId
     * @return
     */
    @Override
    public String initiatePaypalAuthorization(Long supplierId) {
        SupplierPayPalAuth supplierPayPalRoot = supplierPayPalRootRepo.findById(1L).orElse(null);
        if (supplierPayPalRoot == null) {
            throw new IllegalArgumentException("Supplier not found");
        }

        // update the redirect uri of Default Application [sb-mhmy628874237@business.example.com]
//        String baseRedirectUri = "https://692a-66-253-183-231.ngrok-free.app/api/v1/suppliers/v2/callback";

        String redirectUri = baseRedirectUri; // + supplierId;
        String encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        log.info("redirectUri: " + redirectUri);

        supplierPayPalRoot.setPaypalRedirectUri(redirectUri);
        supplierPayPalRootRepo.save(supplierPayPalRoot);

        String state = encryptOrEncodeSupplierId(supplierId); // method to encrypt or encode the supplier ID
        String authUrl = "https://www.sandbox.paypal.com/signin/authorize?client_id="
                + supplierPayPalRoot.getPaypalClientId()
                + "&response_type=code"
                + "&redirect_uri=" + encodedRedirectUri
                + "&scope=openid profile email"
                + "&state=" + state;

        log.info("authUrl: " + authUrl);
        return authUrl;
    }

    @Override
    public String completePaypalAuthorization(
            String authorizationCode, String state) {

        Long supplierId = decryptOrDecodeSupplierId(state);
        SupplierPayPalAuth supplierPayPalRoot = supplierPayPalRootRepo.findById(1L).orElse(null);
        if (supplierPayPalRoot == null) {
            throw new IllegalArgumentException("Supplier not found");
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(supplierPayPalRoot.getPaypalClientId(), supplierPayPalRoot.getPaypalClientSecret());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        String requestBody = null;
        try {
            requestBody = "grant_type=authorization_code&code=" + authorizationCode
                    + "&redirect_uri=" + URLEncoder.encode(supplierPayPalRoot.getPaypalRedirectUri(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "https://api-m.sandbox.paypal.com/v1/oauth2/token",
                HttpMethod.POST, request, String.class);
        log.info("PayPal OAuth token exchange request: " + request + "; response: " + response);

        if (response.getStatusCode() == HttpStatus.OK) {
            // Extract and update the access token from the response
            String accessToken = extractAccessToken(response.getBody());
            log.info("Authorization completed. Access Token for supplier : " +  " " + accessToken);
            updatePaypalAccessToken(supplierId, accessToken);
            return extractAccessToken(response.getBody());
        } else {
            throw new RuntimeException("Failed to exchange authorization code");
        }
    }

    @Override
    public void updatePaypalAccessToken(Long supplierId, String accessToken) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + supplierId));
        supplier.setPaypalAccessToken(accessToken);
        supplierRepository.save(supplier);
    }

    @Override
    public Optional<Object> getPayPalInfo(String accessToken, Long supplierId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = "https://api-m.sandbox.paypal.com/v1/identity/openidconnect/userinfo?schema=openid";

        try {
            ResponseEntity<PayPalInfo> response = restTemplate.exchange(url, HttpMethod.GET, entity, PayPalInfo.class);
            log.info("response: " + response.getBody());

            // update Supplier Entity to add Paypal info
            updatePayPalNameAndEmail(supplierId, response.getBody());

            return Optional.ofNullable(response.getBody());
        } catch (Exception e) {
            log.error("ERROR when getting paypal info!!", e.getMessage());
            e.printStackTrace();
//            return Optional.empty();
            return Optional.of("Error when retrieving PayPal information: " + e.getMessage());
        }
    }

    // update Supplier Entity to add Paypal info
    private void updatePayPalNameAndEmail(Long supplierId, PayPalInfo body) {
        try {
            Supplier supplier = supplierRepository.findById(supplierId).orElseThrow();
            supplier.setPaypalEmail(body.getEmail());
            supplier.setPaypalName(body.getName());
            supplierRepository.save(supplier);
        } catch (DataIntegrityViolationException e) {
//            e.printStackTrace();
//            log.error("Error: Duplicate name or email. Please use unique values.");
            throw new DataIntegrityViolationException("Error: Duplicate name or email. Please use unique values.");
        }
    }

    @Override
    public void updateClientIdAndSecret(Long supplierId, PaypalConfigurationDto configuration) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow();

        supplier.setPaypalClientId(configuration.getPaypalClientId());
        supplier.setPaypalClientSecret(configuration.getPaypalClientSecret()); // Encrypt this

        supplierRepository.save(supplier);

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

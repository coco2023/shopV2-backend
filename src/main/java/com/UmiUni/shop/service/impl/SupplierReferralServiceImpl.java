package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.service.SupplierReferralService;
import com.paypal.base.rest.APIContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
public class SupplierReferralServiceImpl implements SupplierReferralService {

    // PayPal SDK setup and credentials
    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String clientSecret;

    @Value("${paypal.mode}")
    private String mode;

    private String baseUrl = "http:localhost:9011/api/v1/suppliers/refer";

    private final RestTemplate restTemplate;

    @Autowired
    public SupplierReferralServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private APIContext getAPIContext() {
        return new APIContext(clientId, clientSecret, mode);

    }

    @Override
    public String createPartnerReferral() {
//        String url = baseUrl + "/v2/customer/partner-referrals";
        String url = "https://api-m.sandbox.paypal.com/v2/customer/partner-referrals";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBasicAuth(clientId, clientSecret);

        // Construct the request body
        String requestBody = constructRequestBody();

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        return response.getBody();
    }

    private String constructRequestBody() {
        // Construct the JSON body as per PayPal API requirements
        return "{"
                + "\"tracking_id\": \"YOUR_TRACKING_ID\","
                + "\"operations\": [{"
                + "    \"operation\": \"API_INTEGRATION\","
                + "    \"api_integration_preference\": {"
                + "        \"rest_api_integration\": {"
                + "            \"integration_method\": \"PAYPAL\","
                + "            \"integration_type\": \"THIRD_PARTY\","
                + "            \"third_party_details\": {"
                + "                \"features\": [\"PAYMENT\", \"REFUND\"]"
                + "            }"
                + "        }"
                + "    }"
                + "}],"
                + "\"products\": [\"EXPRESS_CHECKOUT\"],"
                + "\"legal_consents\": [{"
                + "    \"type\": \"SHARE_DATA_CONSENT\","
                + "    \"granted\": true"
                + "}]"
                + "}";
    }

}

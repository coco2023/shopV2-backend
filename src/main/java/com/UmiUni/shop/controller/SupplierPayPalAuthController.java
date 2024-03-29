package com.UmiUni.shop.controller;

import com.UmiUni.shop.dto.PaypalConfigurationDto;
import com.UmiUni.shop.entity.Supplier;
import com.UmiUni.shop.service.SupplierService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;

@RestController
@RequestMapping("/api/v1/suppliers")
@Log4j2
public class SupplierPayPalAuthController {

    @Autowired
    private SupplierService supplierService;

    @Value("${paypal.frontend.redirect.uri}") // paypal.frontend.redirect.uri // paypal.frontend.redirect.test
    private String frontendRedirectUri;

    @Autowired
    private ControllerUtli controllerUtli;

    /**
     * Oauth of paypal to the platform
     */
    // http://localhost:9001/api/v1/suppliers/v2/authorize/1
    @GetMapping("/v2/authorize/{supplierId}")
    public ResponseEntity<?> initiateAuthorization(@PathVariable Long supplierId, HttpServletResponse response) throws IOException {
        String redirectUrl = supplierService.initiatePaypalAuthorization(supplierId);
        response.sendRedirect(redirectUrl);
        log.info("response: " + response);
        return ResponseEntity.status(HttpStatus.SEE_OTHER).body("Redirecting to PayPal...");
    }
    @GetMapping("/v2/authorize")
    public ResponseEntity<?> initiateAuthorizationByToken(@RequestHeader("Authorization") String authorizationHeader, HttpServletResponse response) throws IOException {
        Long supplierId = controllerUtli.getSupplierIdByToken(authorizationHeader);
        String redirectUrl = supplierService.initiatePaypalAuthorization(supplierId);
        response.sendRedirect(redirectUrl);
        log.info("response: " + response);
        return ResponseEntity.status(HttpStatus.SEE_OTHER).body("Redirecting to PayPal...");
    }

    // http://localhost:9001/api/v1/suppliers/v2/callback
    @GetMapping("/v2/callback")
    public ResponseEntity<?> completeAuthorization(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpServletResponse response
    ) {
        Long supplierId = decryptOrDecodeSupplierId(state);

        String accessToken = supplierService.completePaypalAuthorization(code, state);
//        supplierService.updatePaypalAccessToken(supplierId, accessToken);

        try {
//            response.sendRedirect("http://localhost:3000/supplier/" + supplierId + "?success=true");
//            response.sendRedirect("http://localhost:3000/supplier-ims/" + supplierId);
            response.sendRedirect(frontendRedirectUri + supplierId);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.ok("Authorization completed. Access Token for supplier : " +  " " + accessToken);
    }

    public Long decryptOrDecodeSupplierId(String encodedId) {
        return Long.parseLong(new String(Base64.getUrlDecoder().decode(encodedId)));
    }

    /**
     * get paypal info of the supplier
     */
    // check if accessToken exit
    // http://localhost:9001/api/v1/suppliers/v2/paypal-info/accessTokenExit/${supplierId}
    @GetMapping("/v2/paypal-info/accessTokenExit/{supplierId}")
    public ResponseEntity<Boolean> checkAccessTokenExit(@PathVariable Long supplierId) {
        // Retrieve the access token for the supplier
        Supplier supplier = supplierService.getSupplier(supplierId);
        String accessToken = supplier.getPaypalAccessToken();
        if (accessToken == null) {
            return ResponseEntity.ok(false);
        }
        return ResponseEntity.ok(true);
    }

    // http://localhost:9001/api/v1/suppliers/v2/paypal-info/${supplierId}
    @GetMapping("/v2/paypal-info/{supplierId}")
    public ResponseEntity<?> getSupplierPaypalInfo(@PathVariable Long supplierId) {
        // Retrieve the access token for the supplier
        Supplier supplier = supplierService.getSupplier(supplierId);
        String accessToken = supplier.getPaypalAccessToken();
        if (accessToken == null) {
            return ResponseEntity.notFound().build();
        }

//        return supplierService.getPayPalInfo(accessToken, supplierId)
//                .map(ResponseEntity::ok)
//                .orElseGet(() -> ResponseEntity.notFound().build());
        return ResponseEntity.ok(supplierService.getPayPalInfo(accessToken, supplierId));
    }

    /**
     * update supplier ClientId and ClientSecret
     * @param supplierId
     * @param configuration
     * @return
     */
    // http://localhost:9001/api/v1/suppliers/v2/suppliers/configure-paypal/1
    // set the PaypalClientId and the PaypalClientSecret
    @PostMapping("/v2/suppliers/configure-paypal/{supplierId}")
    public ResponseEntity<?> configurePaypal(@PathVariable Long supplierId,
                                             @RequestBody PaypalConfigurationDto configuration) {
        supplierService.updateClientIdAndSecret(supplierId, configuration);

        return ResponseEntity.ok("PayPal configuration updated successfully");
    }
    @PostMapping("/v2/suppliers/configure-paypal")
    public ResponseEntity<?> configurePaypalByToken(@RequestHeader("Authorization") String authorizationHeader,
                                             @RequestBody PaypalConfigurationDto configuration) {
        Long supplierId = controllerUtli.getSupplierIdByToken(authorizationHeader);
        supplierService.updateClientIdAndSecret(supplierId, configuration);

        return ResponseEntity.ok("PayPal configuration updated successfully");
    }


}

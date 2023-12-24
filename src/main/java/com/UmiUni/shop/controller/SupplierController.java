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
import java.net.URLEncoder;
import java.util.List;

@RestController
@RequestMapping("/api/v1/suppliers")
@Log4j2
public class SupplierController {

    @Value("${paypal.redirect.uri}")
    private String defaultRedirectUri;

    @Autowired
    private SupplierService supplierService;

    @PostMapping
    public ResponseEntity<Supplier> createSupplier(@RequestBody Supplier supplier) {
        return ResponseEntity.ok(supplierService.createSupplier(supplier));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getSupplierById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getSupplier(id));
    }

    @GetMapping("/all")
    public List<Supplier> getAllSuppliers() {
        return supplierService.getAllSuppliers();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Supplier> updateSupplier(@PathVariable Long id, @RequestBody Supplier supplierDetails) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, supplierDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.ok().build();
    }

    // http://localhost:9001/api/v1/suppliers/paypal/authorize/1/update-default-RedirectUri
    @PostMapping("/paypal/authorize/{supplierId}/update-default-RedirectUri")
    public ResponseEntity<?> updateSupplierRedirectUri(@PathVariable Long supplierId) {
        Supplier supplier = supplierService.getSupplier(supplierId);
        if (supplier == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Supplier not found");
        }
        supplier.setPaypalRedirectUri(defaultRedirectUri);
        supplierService.createSupplier(supplier);
        return ResponseEntity.ok("Redirect URI updated successfully");
    }

    /**
     * v2
     */
    // http://localhost:9001/api/v1/suppliers/v2/suppliers/configure-paypal/1
    // set the PaypalClientId and the PaypalClientSecret
    @PostMapping("/v2/suppliers/configure-paypal/{supplierId}")
    public ResponseEntity<?> configurePaypal(@PathVariable Long supplierId,
                                             @RequestBody PaypalConfigurationDto configuration) {
        Supplier supplier = supplierService.getSupplier(supplierId);
        if (supplier == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Supplier not found");
        }

        supplier.setPaypalClientId(configuration.getPaypalClientId());
        supplier.setPaypalClientSecret(configuration.getPaypalClientSecret()); // Encrypt this
        supplier.setPaypalRedirectUri(configuration.getPaypalRedirectUri());

        supplierService.createSupplier(supplier);
        return ResponseEntity.ok("PayPal configuration updated successfully");
    }

    // http://localhost:9001/api/v1/suppliers/v2/authorize/1
    @GetMapping("/v2/authorize/{supplierId}")
    public ResponseEntity<?> initiateAuthorization(@PathVariable Long supplierId, HttpServletResponse response) throws IOException {
        String redirectUrl = supplierService.initiatePaypalAuthorization(supplierId);
        response.sendRedirect(redirectUrl);
        log.info("response: " + response);
        return ResponseEntity.status(HttpStatus.SEE_OTHER).body("Redirecting to PayPal...");
    }

    // http://localhost:9001/api/v1/suppliers/v2/callback/1
    @GetMapping("/v2/callback")
    public ResponseEntity<?> completeAuthorization(@RequestParam("code") String code, @RequestParam("state") String state) {
        String accessToken = supplierService.completePaypalAuthorization(code, state);
        log.info("Authorization completed. Access Token for supplier : " +  " " + accessToken);
        return ResponseEntity.ok("Authorization completed. Access Token for supplier : " +  " " + accessToken);
    }

}

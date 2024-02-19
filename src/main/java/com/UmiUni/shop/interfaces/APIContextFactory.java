package com.UmiUni.shop.interfaces;

import com.UmiUni.shop.entity.Supplier;
import com.UmiUni.shop.repository.SupplierRepository;
import com.paypal.base.rest.APIContext;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class APIContextFactory {

    @Autowired
    private SupplierRepository supplierRepository;

    public APIContext createApiContextForSupplier(Long supplierId) {
        // Logic to create and configure APIContext based on the supplierId
        log.info("supplierId: {}", supplierId);
        Supplier supplier = supplierRepository.findById(61L)  // 61L  // supplierId
                .orElseThrow(() -> new RuntimeException("Supplier not found: " + supplierId));
        String supplierClientId = supplier.getPaypalClientId();
        String supplierSecret = supplier.getPaypalClientSecret();

        APIContext apiContext = new APIContext(supplierClientId, supplierSecret, "sandbox");
        log.info("success!");
        return apiContext;
    }

}

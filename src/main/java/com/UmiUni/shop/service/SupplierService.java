package com.UmiUni.shop.service;

import com.UmiUni.shop.dto.PaypalConfigurationDto;
import com.UmiUni.shop.entity.Supplier;
import com.UmiUni.shop.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;

public interface SupplierService {

    Supplier createSupplier(Supplier supplier);

    Supplier getSupplier(Long id);

    List<Supplier> getAllSuppliers();

    Supplier updateSupplier(Long id, Supplier supplierDetails);

    void deleteSupplier(Long id);

    String initiatePaypalAuthorization(Long supplierId);

    String completePaypalAuthorization(String code, String state);

    void updatePaypalAccessToken(Long supplierId, String accessToken);

    Optional<Object> getPayPalInfo(String accessToken, Long supplierId);

    void updateClientIdAndSecret(Long supplierId, PaypalConfigurationDto configuration);

    Supplier getSupplierByName(String username);
}

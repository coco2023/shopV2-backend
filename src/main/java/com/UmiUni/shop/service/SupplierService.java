package com.UmiUni.shop.service;

import com.UmiUni.shop.entity.Supplier;
import com.UmiUni.shop.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface SupplierService {

    Supplier createSupplier(Supplier supplier);

    Supplier getSupplier(Long id);

    List<Supplier> getAllSuppliers();

    Supplier updateSupplier(Long id, Supplier supplierDetails);

    void deleteSupplier(Long id);

    String initiatePaypalAuthorization(Long supplierId);

    String completePaypalAuthorization(String code, String state);
}

package com.UmiUni.shop.service;

import com.UmiUni.shop.entity.Supplier;
import com.UmiUni.shop.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SupplierService {

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
        // other updates as needed
        return supplierRepository.save(supplier);
    }

    public void deleteSupplier(Long id) {
        supplierRepository.deleteById(id);
    }
}

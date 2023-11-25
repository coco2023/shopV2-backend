package com.UmiUni.shop.service;

import com.UmiUni.shop.entity.Brand;

import com.UmiUni.shop.repository.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BrandService {

    @Autowired
    private BrandRepository brandRepository;

    public Brand createBrand(Brand brand) {
        return brandRepository.save(brand);
    }

    public Brand getBrand(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found with id: " + id));
    }

    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    public Brand updateBrand(Long id, Brand brandDetails) {
        Brand brand = getBrand(id);
        brand.setBrandName(brandDetails.getBrandName());
        // other updates as needed
        return brandRepository.save(brand);
    }

    public void deleteBrand(Long id) {
        brandRepository.deleteById(id);
    }
}

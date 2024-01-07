package com.UmiUni.shop.security.service.impl;

import com.UmiUni.shop.entity.Customer;
import com.UmiUni.shop.entity.Supplier;
import com.UmiUni.shop.repository.CustomerRepository;
import com.UmiUni.shop.repository.SupplierRepository;
import com.UmiUni.shop.security.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class UserServiceImpl implements UserService {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public Supplier registerOrUpdateSupplier(OAuth2User oAuth2User) {
        // Extract information from oAuth2User
        log.info("supplier OAuth2User: " + oAuth2User.getAttributes());
        String email = oAuth2User.getAttribute("email");
        Supplier supplier = supplierRepository.findByPaypalEmail(email)
                .orElse(new Supplier());
        // Set or update other fields from OAuth2User
        supplier.setPaypalEmail(email);
        supplier.setPaypalName(oAuth2User.getAttribute("name"));
        // ... other fields ...
        return supplierRepository.save(supplier);}

    @Override
    public Customer registerOrUpdateCustomer(OAuth2User oAuth2User) {
        log.info("customer OAuth2User: " + oAuth2User.getAttributes());
        String email = oAuth2User.getAttribute("email");
        Customer customer = customerRepository.findByPaypalEmail(email)
                .orElse(new Customer());
        // Set or update other fields from OAuth2User
        customer.setPaypalEmail(email);
        customer.setPaypalName(oAuth2User.getAttribute("name"));
        // ... other fields ...
        return customerRepository.save(customer);
    }

}

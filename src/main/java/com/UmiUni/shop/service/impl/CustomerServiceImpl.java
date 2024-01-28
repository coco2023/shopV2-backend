package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.entity.Customer;
import com.UmiUni.shop.repository.CustomerRepository;
import com.UmiUni.shop.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    @Override
    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public Customer updateCustomer(Long id, Customer customerDetails) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found for this id :: " + id));
        customer.setCustomerName(customerDetails.getCustomerName());
        customer.setContactInfo(customerDetails.getContactInfo());
        customer.setEmail(customerDetails.getEmail());
        customer.setName(customerDetails.getName());
        customer.setPassword(customerDetails.getPassword());
        customer.setPaypalEmail(customerDetails.getPaypalEmail());
        customer.setPaypalName(customerDetails.getPaypalName());
        customer.setPaypalAccessToken(customerDetails.getPaypalAccessToken());
        customer.setBalance(customerDetails.getBalance());
        customer.setUserType(customerDetails.getUserType());
        return customerRepository.save(customer);
    }

    @Override
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found for this id :: " + id));
        customerRepository.delete(customer);
    }
}

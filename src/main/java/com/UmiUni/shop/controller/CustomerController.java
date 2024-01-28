package com.UmiUni.shop.controller;

import com.UmiUni.shop.entity.Customer;
import com.UmiUni.shop.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping()
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        return ResponseEntity.ok().body(customerService.saveCustomer(customer));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable(value = "id") Long customerId) {
        Customer customer = customerService.getCustomerById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found for this id :: " + customerId));
        return ResponseEntity.ok().body(customer);
    }

    @GetMapping("/all")
    public List<Customer> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable(value = "id") Long customerId, @RequestBody Customer customerDetails) {
        Customer updatedCustomer = customerService.updateCustomer(customerId, customerDetails);
        return ResponseEntity.ok(updatedCustomer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable(value = "id") Long customerId) {
        customerService.deleteCustomer(customerId);
        return ResponseEntity.ok().build();
    }
}

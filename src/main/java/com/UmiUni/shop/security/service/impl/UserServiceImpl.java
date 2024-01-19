package com.UmiUni.shop.security.service.impl;

import com.UmiUni.shop.constant.UserType;
import com.UmiUni.shop.entity.Customer;
import com.UmiUni.shop.entity.Employee;
import com.UmiUni.shop.entity.Supplier;
import com.UmiUni.shop.repository.CustomerRepository;
import com.UmiUni.shop.repository.EmployeeRepository;
import com.UmiUni.shop.repository.SupplierRepository;
import com.UmiUni.shop.security.dto.RegistrationRequestDTO;
import com.UmiUni.shop.security.dto.RegistrationResponseDTO;
import com.UmiUni.shop.security.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
public class UserServiceImpl implements UserService {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Supplier registerOrUpdateSupplier(OAuth2User oAuth2User) {
        // Extract information from oAuth2User
        String email = oAuth2User.getAttribute("email");
        Supplier supplier = supplierRepository.findByPaypalEmail(email)
                .orElse(null);
        if (supplier == null) {
            supplier = Supplier.builder()
                    .supplierName(oAuth2User.getAttribute("name"))
                    .paypalEmail(email)
                    .build();
        } else {
            // Set or update other fields from OAuth2User
            supplier.setPaypalEmail(email);
            supplier.setPaypalName(oAuth2User.getAttribute("name"));
        }
        return supplierRepository.save(supplier);}

    @Override
    public Customer registerOrUpdateCustomer(OAuth2User oAuth2User) {
        log.info("OAuth2User: " + oAuth2User.getAttributes());
        String email = oAuth2User.getAttribute("email");
        Customer customer = customerRepository.findByPaypalEmail(email)
                .orElse(new Customer());
        // Set or update other fields from OAuth2User
        customer.setPaypalEmail(email);
        customer.setPaypalName(oAuth2User.getAttribute("name"));
        // ... other fields ...
        return customerRepository.save(customer);
    }

    /**
     * create new supplier
     * @param registrationRequestDTO
     * @return
     */
    @Transactional
    @Override
    public RegistrationResponseDTO registerNewUser(RegistrationRequestDTO registrationRequestDTO) {

        // Check if username exists
        if (supplierRepository.existsBySupplierName(registrationRequestDTO.getUsername())) {
            throw new RuntimeException("Username is already taken!"); //CustomDuplicateUsernameException
        }

        // Check if email exists
        if (supplierRepository.existsByContactInfo(registrationRequestDTO.getEmail())) {
            throw new RuntimeException("Email is already in use!"); // CustomDuplicateEmailException
        }

        // Hash the password
        String hashedPassword = passwordEncoder.encode(registrationRequestDTO.getPassword());

        // Assign a role to the new user
        String role = registrationRequestDTO.getRoleName();

        RegistrationResponseDTO registrationResponseDTO = null;
        if (role.equals("SUPPLIER")) {
            // Create a new user
            Supplier newUser = new Supplier();
            newUser.setSupplierName(registrationRequestDTO.getUsername());
            newUser.setPassword(hashedPassword);
            newUser.setContactInfo(registrationRequestDTO.getEmail());
            newUser.setUserType(role);
            newUser = supplierRepository.save(newUser);

            registrationResponseDTO = RegistrationResponseDTO.builder()
                    .userID(newUser.getSupplierId())
                    .username(newUser.getSupplierName())
                    .userType(newUser.getUserType())
                    .build();

        } else if (role.equals("CUSTOMER")) {
            // Create a new user
            Customer newUser = new Customer();
            newUser.setName(registrationRequestDTO.getUsername());
            newUser.setEmail(registrationRequestDTO.getEmail());
            newUser.setPassword(hashedPassword);
            newUser.setContactInfo(registrationRequestDTO.getEmail());
            newUser.setUserType(role);
            newUser = customerRepository.save(newUser);

            registrationResponseDTO = RegistrationResponseDTO.builder()
                    .userID(newUser.getId())
                    .username(newUser.getName())
                    .userType(newUser.getUserType())
                    .build();
        } else { // if (role.equals("ADMIN") or "TESTSER") {
            Employee newUser = new Employee();
            newUser.setName(registrationRequestDTO.getUsername());
            newUser.setEmail(registrationRequestDTO.getEmail());
            newUser.setPassword(hashedPassword);
            newUser.setEmail(registrationRequestDTO.getEmail());
            newUser.setUserType(role);
            newUser = employeeRepository.save(newUser);

            registrationResponseDTO = RegistrationResponseDTO.builder()
                    .userID(newUser.getId())
                    .username(newUser.getName())
                    .userType(newUser.getUserType())
                    .build();
        }

        return registrationResponseDTO;
    }

    /**
     * auth part for the login user
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // should determine the user type first
        // Retrieve the user and their roles
        Supplier user = supplierRepository.findBySupplierName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getUserType()));

        // Return the user details including the authorities
        return new org.springframework.security.core.userdetails.User(
                user.getSupplierName(),
                user.getPassword(),
                authorities
        );

//        Customer user = customerRepository.findByName(username);
////                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
//
//        List<GrantedAuthority> authorities = new ArrayList<>();
//        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getUserType()));
//
//        // Return the user details including the authorities
//        return new org.springframework.security.core.userdetails.User(
//                user.getName(),
//                user.getPassword(),
//                authorities
//        );
    }

}

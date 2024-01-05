package com.UmiUni.shop.security.service;

import com.UmiUni.shop.entity.Customer;
import com.UmiUni.shop.entity.Supplier;
import com.UmiUni.shop.security.dto.RegistrationRequestDTO;
import com.UmiUni.shop.security.dto.RegistrationResponseDTO;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface UserService extends UserDetailsService {
    Supplier registerOrUpdateSupplier(OAuth2User oAuth2User);

    Customer registerOrUpdateCustomer(OAuth2User oAuth2User);

    RegistrationResponseDTO registerNewUser(RegistrationRequestDTO registrationRequestDTO);
}

package com.UmiUni.shop.security.service;

import com.UmiUni.shop.entity.Customer;
import com.UmiUni.shop.entity.Supplier;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface UserService {
    Supplier registerOrUpdateSupplier(OAuth2User oAuth2User);
    Customer registerOrUpdateCustomer(OAuth2User oAuth2User);
}

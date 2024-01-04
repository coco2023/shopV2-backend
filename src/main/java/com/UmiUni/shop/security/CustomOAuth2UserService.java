package com.UmiUni.shop.security;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class CustomOAuth2UserService extends DefaultOAuth2UserService  {

    @Autowired
    private UserService userService; // UserService should be defined by you to handle user data

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        UserType userType = determineUserType(oAuth2User);

        if (userType == UserType.SUPPLIER) {
            userService.registerOrUpdateSupplier(oAuth2User);
        } else if (userType == UserType.CUSTOMER) {
            userService.registerOrUpdateCustomer(oAuth2User);
        }

        // Return the OAuth2User or a custom UserDetails implementation
        return oAuth2User;
    }

    enum UserType {
        SUPPLIER, CUSTOMER
    }

    private UserType determineUserType(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        if (email != null) {
            if (email.endsWith("@business.example.com")) {
                return UserType.SUPPLIER;
            } else {
                return UserType.CUSTOMER;
            }
        } else {
            throw new IllegalArgumentException("Unable to determine user type: Email is missing");
        }
    }

}

package com.UmiUni.shop.security.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Log4j2
public class CustomOAuth2UserService extends DefaultOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Autowired
    private UserService userService; // UserService should be defined by you to handle user data

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // Implement logic to determine if the OAuth2User is a supplier or customer
        // This is application specific. For example, you might differentiate them by a specific attribute

        // Assuming you have a method to determine the user type
        log.info("oAuth2User: " + oAuth2User.toString());
        UserType userType = determineUserType(oAuth2User);

        if (userType == UserType.SUPPLIER) {
            userService.registerOrUpdateSupplier(oAuth2User);
        } else if (userType == UserType.CUSTOMER) {
            userService.registerOrUpdateCustomer(oAuth2User);
        }

        // Return the OAuth2User or a custom UserDetails implementation
        return oAuth2User;
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

        // not work
//        String userGroup = oAuth2User.getAttribute("user_group");
//        log.info("userGroup: " + userGroup);
//
//        if ("supplier".equals(userGroup)) {
//            return UserType.SUPPLIER;
//        } else if ("customer".equals(userGroup)) {
//            return UserType.CUSTOMER;
//        } else {
//            // Default or error handling
//            throw new IllegalArgumentException("Unknown user type");
//        }
        // Return UserType.SUPPLIER or UserType.CUSTOMER
    }

    enum UserType {
        SUPPLIER, CUSTOMER
    }

//    private OAuth2User processOAuth2User(OAuth2User oAuth2User) {
//        // Extract required details from OAuth2User
//        String email = oAuth2User.getAttribute("email");
//        if (email == null) {
//            throw new OAuth2AuthenticationException(new OAuth2Error("email_not_found"), "Email not found from OAuth2 provider");
//        }
//
//        // Determine user type (supplier or customer)
//        String userType = determineUserType(oAuth2User.getAttributes());
//
//        if ("supplier".equals(userType)) {
//            userService.registerOrUpdateSupplier(oAuth2User);
//        } else if ("customer".equals(userType)) {
//            userService.registerOrUpdateCustomer(oAuth2User);
//        } else {
//            // Handle the case where user type cannot be determined
//            throw new OAuth2AuthenticationException(new OAuth2Error("user_type_not_determined"), "User type could not be determined");
//        }
//
//        // Create and return UserPrincipal (adapt this according to your implementation)
//        return UserPrincipal.create(oAuth2User, (UserServiceImpl) oAuth2User.getAttributes());
//    }
//
//    private String determineUserType(Map<String, Object> attributes) {
//        // Implement your logic to determine user type based on attributes
//        // Example logic (placeholder)
//        return "customer"; // or "supplier"
//    }
}

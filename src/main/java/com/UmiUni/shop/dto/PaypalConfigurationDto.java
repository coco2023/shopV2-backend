package com.UmiUni.shop.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class PaypalConfigurationDto {

    private Long supplierId;

    private String supplierName;

    private String contactInfo;

    private String paypalEmail; // PayPal account email

//    private String paypalAccessToken; // Store PayPal access token

    private String paypalClientId;

    private String paypalClientSecret; // Ensure this is stored securely

    private String paypalRedirectUri;

}

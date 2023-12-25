package com.UmiUni.shop.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
@Table(name = "Suppliers")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long supplierId;

    @Column(nullable = false)
    private String supplierName;

    private String contactInfo;

    private String paypalEmail; // PayPal account email

    private String paypalAccessToken; // Store PayPal access token

//    private String paypalClientId;
//
//    @Column(length = 500)
//    private String paypalClientSecret; // Ensure this is stored securely
//
//    private String paypalRedirectUri;

}

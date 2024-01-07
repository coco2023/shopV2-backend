package com.UmiUni.shop.entity;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

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

    @Column(unique = true)
    private String paypalEmail; // PayPal account email

    private String paypalName; // PayPal account email

    @Column(unique = true)
    private String paypalAccessToken; // Store PayPal access token

    @Column(unique = true)
    private String paypalClientId;

    @Column(length = 500, unique = true)
    private String paypalClientSecret; // Ensure this is stored securely

    private String paypalRedirectUri;

    private BigDecimal balance;

    private String userType;
}

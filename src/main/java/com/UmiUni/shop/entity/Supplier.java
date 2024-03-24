package com.UmiUni.shop.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
@Table(name = "Suppliers")
@DiscriminatorValue("SUPPLIER")
public class Supplier extends User {

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long supplierId;

    @Column(nullable = false, unique = true)
    @Pattern(regexp = "\\S+", message = "Supplier Name must not contain spaces")
    private String supplierName;

    private String contactInfo;

    @Column(unique = true)
    private String paypalEmail; // PayPal account email

    private String paypalName; // PayPal account name

    @Column(unique = true)
    private String paypalAccessToken; // Store PayPal access token

    @Column(unique = true)
    private String paypalClientId;

    @Column(length = 500, unique = true)
    private String paypalClientSecret; // Ensure this is stored securely

    private String paypalRedirectUri;

    private BigDecimal balance;

    private String userType;

    public void setSupplierName(String supplierName) {
        if(supplierName.contains(" ")) {
            throw new IllegalArgumentException("Supplier Name must not contain spaces");
        }
        this.supplierName = supplierName;
    }

}

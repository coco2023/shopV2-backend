package com.UmiUni.shop.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
@Table(name = "SupplierPayPalAuth")
public class SupplierPayPalAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long supplierId;

    private String paypalClientId;

    @Column(length = 500)
    private String paypalClientSecret; // Ensure this is stored securely

    private String paypalRedirectUri;

}

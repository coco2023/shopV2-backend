package com.UmiUni.shop.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
@Table(name = "ProductAttributes")
public class ProductAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attributeId;

    private Long productId;

    private String skuCode;

    @Column(nullable = false)
    private String attributeName;

    private String attributeValue;
}
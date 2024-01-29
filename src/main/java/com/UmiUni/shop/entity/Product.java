package com.UmiUni.shop.entity;

import com.UmiUni.shop.constant.StockStatus;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
@Table(name = "Products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    @Column(unique = true)
    private String skuCode;

    private Long categoryId;
    private String categoryName;

    private Long brandId;
    private String brandName;

    private Long supplierId;
    private String supplierName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private BigDecimal discount;

    private BigDecimal finalPrice;

    private Double rating; // a rating is a number from 1 to 5

    private Long salesAmount; // the total number of sales

    private String imageUrl;

    @ElementCollection
//    @CollectionTable(name = "product_image_ids", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_id")
    private List<Long> productImageIds;

    private Integer stockQuantity;

    @Enumerated(EnumType.STRING)
    private StockStatus stockStatus;

    private String shippingInfo;

    private LocalDateTime lastStockUpdate;

    private Integer lockedStockQuantity; // to track locked (reserved) inventory

//    @Version
//    private Long version; // for optimistic locking
}

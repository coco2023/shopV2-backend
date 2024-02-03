package com.UmiUni.shop.dto;

import com.UmiUni.shop.constant.StockStatus;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class ProductDTO implements Serializable {

    private static final long serialVersionUID = 7727871091681128507L;

    private Long productId;

    private String productName;

    private String skuCode;

    private Long categoryId;
    private String categoryName;

    private Long brandId;
    private String brandName;

    private Long supplierId;
    private String supplierName;

    private String description;

    private BigDecimal price;

    private BigDecimal discount;

    private BigDecimal finalPrice;

    private Double rating; // a rating is a number from 1 to 5

    private Long salesAmount; // the total number of sales

    private String imageUrl;

    private List<Long> productImageIds;

    private Integer stockQuantity;

    private StockStatus stockStatus;

    private String shippingInfo;

    private LocalDateTime lastStockUpdate;

    private Integer lockedStockQuantity; // to track locked (reserved) inventory

}

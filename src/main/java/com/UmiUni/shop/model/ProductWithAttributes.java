package com.UmiUni.shop.model;

import com.UmiUni.shop.constant.StockStatus;
import com.UmiUni.shop.entity.ProductAttribute;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class ProductWithAttributes {

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

    private String imageUrl;

    private Integer stockQuantity;

    private StockStatus stockStatus;

    private String shippingInfo;

    private LocalDateTime lastStockUpdate;

    private List<ProductAttribute> productAttributeList;

}

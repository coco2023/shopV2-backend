package com.UmiUni.shop.model;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class InventoryUpdateMessage implements Serializable {

    private static final long serialVersionUID = 1L;
    private String skuCode;
    private int quantity;
}

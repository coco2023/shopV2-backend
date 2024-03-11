package com.UmiUni.shop.mq.notification.model;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class NotificationMessage implements Serializable {

    private String supplierId;

    private String skuCode;

    private int quantity;
}

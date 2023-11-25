package com.UmiUni.shop.model;

import com.UmiUni.shop.entity.SalesOrder;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class StripePaymentRequest {

    private String token;

    private SalesOrder salesOrder;

}

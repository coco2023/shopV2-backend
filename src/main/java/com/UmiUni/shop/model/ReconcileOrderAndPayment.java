package com.UmiUni.shop.model;

import com.UmiUni.shop.entity.PayPalPayment;
import com.UmiUni.shop.entity.SalesOrder;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class ReconcileOrderAndPayment {

    private PayPalPayment payPalPayment;

    private SalesOrder salesOrder;

    private String reconcileErrorLog;

}

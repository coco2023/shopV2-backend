package com.UmiUni.shop.model;


import com.UmiUni.shop.constant.PaymentMethod;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class PaymentStatusResponse {
    private String status;

    private String errorDetails;

    private double amount;

    private PaymentMethod paymentMethod;
}

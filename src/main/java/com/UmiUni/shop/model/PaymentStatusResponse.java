package com.UmiUni.shop.model;


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

}

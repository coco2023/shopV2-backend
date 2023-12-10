package com.UmiUni.shop.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class PaymentResponse {

    private String status;

    private String transactionId;
}

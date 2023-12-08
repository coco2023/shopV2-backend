package com.UmiUni.shop.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class PayPalPaymentResponse {

    private String status;

    private String paymentId;

    private String approvalUrl;

}

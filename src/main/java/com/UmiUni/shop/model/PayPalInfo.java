package com.UmiUni.shop.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class PayPalInfo {

    private String email;

    private String name;

}

package com.UmiUni.shop.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class AuthRequest {

    private String username;

    private String password;

}
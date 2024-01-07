package com.UmiUni.shop.security.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class AuthRequest {

    private String username;

    private String password;

    private String roleName;

}
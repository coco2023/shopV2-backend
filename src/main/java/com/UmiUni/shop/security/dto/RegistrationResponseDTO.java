package com.UmiUni.shop.security.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class RegistrationResponseDTO {

    private Integer userID;

    private String username;

    private String userType;

}
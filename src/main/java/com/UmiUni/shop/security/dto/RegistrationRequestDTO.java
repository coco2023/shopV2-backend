package com.UmiUni.shop.security.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class RegistrationRequestDTO {

    private String username;

    private String password; // This should be a raw password that will be hashed in the service layer

    private String email;

    private String roleName; // Assuming the client knows the role names

}
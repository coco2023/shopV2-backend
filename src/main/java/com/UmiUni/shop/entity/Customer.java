package com.UmiUni.shop.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.math.BigDecimal;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString(callSuper = true)
@DiscriminatorValue("CUSTOMER")
public class Customer extends User{

    private String customerName;

    private String contactInfo;

    private String paypalEmail;

    private String paypalName;

    private String paypalAccessToken;

    private BigDecimal balance;
}
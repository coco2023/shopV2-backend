package com.UmiUni.shop.entity;

import com.paypal.api.payments.Payer;
import com.paypal.base.rest.PayPalResource;
import lombok.*;

import javax.persistence.*;

@Entity@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
@Table(name = "PayPalPaymentResponse")
public class PayPalPaymentResponseEntity extends PayPalResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String paymentId;
    private String intent;
    private String state;
    private String createTime;
    private String updateTime;

    // Payer information
    private String payerStatus;
    private String payerEmail;
    private String payerFirstName;
    private String payerLastName;
    private String payerId;
    private String payerCountryCode;

    // Shipping address
    private String shippingRecipientName;
    private String shippingLine1;
    private String shippingCity;
    private String shippingState;
    private String shippingCountryCode;
    private String shippingPostalCode;

    // Transaction information
    private String transactionAmountCurrency;
    private Double transactionAmountTotal;
    private String transactionDescription;
    private String transactionCustom;
    private String transactionSoftDescriptor;

    private String saleId;
    private String saleState;
    private String salePaymentMode;
    private String saleProtectionEligibility;
    private String saleProtectionEligibilityType;
    private String saleCreateTime;
    private String saleUpdateTime;
    private String saleAmountCurrency;
    private Double saleAmountTotal;

    private String saleAmountDetailsSubtotal;
    private String saleAmountDetailsShipping;
    private String saleAmountDetailsHandlingFee;
    private String saleAmountDetailsShippingDiscount;
    private String saleAmountDetailsInsurance;

    // Payment receiver/payee information
    private String payeeEmail;
    private String payeeMerchantId;

    // transactionFee
    private String saleTransactionFeeCurrency;
    private Double saleTransactionFeeValue;

    private String cart;

}

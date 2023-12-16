package com.UmiUni.shop.dto;

import com.UmiUni.shop.entity.PayPalPaymentResponseEntity;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class PayPalPaymentResponseDTO {

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

    // Constructor
    public PayPalPaymentResponseDTO(PayPalPaymentResponseEntity entity) {
        this.id = entity.getId();
        this.paymentId = entity.getPaymentId();
        this.intent = entity.getIntent();
        this.state = entity.getState();
        this.createTime = entity.getCreateTime();
        this.updateTime = entity.getUpdateTime();
        // Payer information
        this.payerStatus = entity.getPayerStatus();
        this.payerEmail = entity.getPayerEmail();
        this.payerFirstName = entity.getPayerFirstName();
        this.payerLastName = entity.getPayerLastName();
        this.payerId = entity.getPayerId();
        this.payerCountryCode = entity.getPayerCountryCode();
        // Shipping address
        this.shippingRecipientName = entity.getShippingRecipientName();
        this.shippingLine1 = entity.getShippingLine1();
        this.shippingCity = entity.getShippingCity();
        this.shippingState = entity.getShippingState();
        this.shippingCountryCode = entity.getShippingCountryCode();
        this.shippingPostalCode = entity.getShippingPostalCode();
        // Transaction information
        this.transactionAmountCurrency = entity.getTransactionAmountCurrency();
        this.transactionAmountTotal = entity.getTransactionAmountTotal();
        this.transactionDescription = entity.getTransactionDescription();
        this.transactionCustom = entity.getTransactionCustom();
        this.transactionSoftDescriptor = entity.getTransactionSoftDescriptor();
        this.saleId = entity.getSaleId();
        this.saleState = entity.getSaleState();
        this.salePaymentMode = entity.getSalePaymentMode();
        this.saleProtectionEligibility = entity.getSaleProtectionEligibility();
        this.saleProtectionEligibilityType = entity.getSaleProtectionEligibilityType();
        this.saleCreateTime = entity.getSaleCreateTime();
        this.saleUpdateTime = entity.getSaleUpdateTime();
        this.saleAmountCurrency = entity.getSaleAmountCurrency();
        this.saleAmountTotal = entity.getSaleAmountTotal();
        this.saleAmountDetailsSubtotal = entity.getSaleAmountDetailsSubtotal();
        this.saleAmountDetailsShipping = entity.getSaleAmountDetailsShipping();
        this.saleAmountDetailsHandlingFee = entity.getSaleAmountDetailsHandlingFee();
        this.saleAmountDetailsShippingDiscount = entity.getSaleAmountDetailsShippingDiscount();
        this.saleAmountDetailsInsurance = entity.getSaleAmountDetailsInsurance();
        // Payment receiver/payee information
        this.payeeEmail = entity.getPayeeEmail();
        this.payeeMerchantId = entity.getPayeeMerchantId();
        // Transaction fee
        this.saleTransactionFeeCurrency = entity.getSaleTransactionFeeCurrency();
        this.saleTransactionFeeValue = entity.getSaleTransactionFeeValue();
        this.cart = entity.getCart();
    }

}

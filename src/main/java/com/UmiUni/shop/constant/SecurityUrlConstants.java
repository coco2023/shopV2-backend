package com.UmiUni.shop.constant;

public class SecurityUrlConstants {
    public static final String[] PUBLIC_READ_URLS = {
            "/api/v1/products/**",
            "/api/v1/productAttributes/**",
            "/api/v1/invoices/**",
            "/api/v1/brands/**",
            "/api/v1/categories/**",
            "/api/v1/salesOrders/**",
            "/api/v1/salesOrderDetails/**",
            "/api/v1/payments/**",
            "/api/v1/payments/paypal/**",
            "/api/v1/payments/stripe/**",
    };

    public static final String[] PUBLIC_CREATE_URLS = {
            "/api/v1/payments/paypal/**",
            "/api/v1/payments/stripe/**",
            "/api/v1/salesOrders/**",
            "/api/v1/salesOrderDetails/**",
    };

    public static final String[] PUBLIC_URLS = {
            "/home", "/login",
            "/",
            "/api/v1/suppliers/v2/**",
            "/api/v1/suppliers/finance/**",
            "/api/v1/reconciliation/**",
            "/login/**",
            "/auth/**",
            "/api/auth/**",
            "/api/auth/login",
            "/api/auth/register",
            "/login/oauth2/code/**",
            "/api/v1/suppliers/all",
            "/api/v1/products/**"
    };

    public static final String[] SWAGGER_WHITELIST = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/api-docs/**",
            "/v2/api-docs/**",
            "/v3/api-docs/**",
            "/webjars/**",
            "/doc.html",
    };

    public static final String H2_CONSOLE = "/h2-console/**";

    public static final String[] ADMIN_PUBLIC_URLS = {
            "/api/v1/suppliers/**",
            "/api/v1/customers/**",
    };

    public static final String[] SUPPLIER_PUBLIC_URLS = {
            "/api/v1/suppliers/auth/info/**",
            "/api/v1/suppliers/products/**",
            "/api/v1/suppliers/salesOrders/**",
            "/api/v1/suppliers/payments/**",
    };

    public static final String[] CUSTOMER_PUBLIC_URLS = {
            "/api/v1/customers/auth/info/**",
            "/api/v1/customers/salesOrders/**"
    };

}

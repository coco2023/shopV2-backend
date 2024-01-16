package com.UmiUni.shop.constant;

public class SecurityUrlConstants {
    public static final String[] READ_ONLY_URLS = {
            "/api/v1/products/**",
            "/api/v1/productAttributes/**",
            "/api/v1/invoices/**",
            "/api/v1/brands/**",
            "/api/v1/categories/**",
            "/api/v1/salesOrders/**",
            "/api/v1/salesOrderDetails/**",
            "/api/v1/payments/**",
    };

    public static final String[] OTHER_PUBLIC_URLS = {
            "/home", "/login",
            "/", "/auth/**",
            "/api/v1/suppliers/v2/**",
            "/api/v1/suppliers/finance/**",
            "/api/v1/reconciliation/**",
            "/login/**",
            String.valueOf(SecurityConstants.SWAGGER_WHITELIST),
            SecurityConstants.H2_CONSOLE,
            "/api/auth/**",
            "/login/oauth2/code/**",
            "/api/v1/suppliers/all"
    };

    public static final String[] SUPPLIER_PUBLIC_URLS = {
            "/api/v1/suppliers/**"
    };
}

package com.UmiUni.shop.config;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

public class JwtConfig {
    public SecretKey generateSecretKey() {
        // Generate the key
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        // You can store the key in a safe location
        return key;
    }
}

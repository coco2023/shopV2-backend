package com.UmiUni.shop;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class ShopApplication {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().load();
        SpringApplication.run(ShopApplication.class, args);
	}

}

package com.UmiUni.shop;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.TimeZone;

@SpringBootApplication(scanBasePackages = {"com.UmiUni.shop"})
@EnableConfigurationProperties
@EnableScheduling
@EnableWebMvc
@EnableRabbit
public class ShopApplication {
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Chicago"));
        SpringApplication.run(ShopApplication.class, args);
	}
}

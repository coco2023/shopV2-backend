package com.UmiUni.shop;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.env.Environment;

@SpringBootApplication
@EnableConfigurationProperties
public class ShopApplication {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().load();
        SpringApplication.run(ShopApplication.class, args);
	}

//    public ShopApplication(Environment environment) {
//        // Load environment variables from .env file
//        Dotenv.configure().directory(".env").ignoreIfMalformed().ignoreIfMissing().load();
//    }

}

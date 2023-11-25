package com.UmiUni.shop;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class ShopApplication extends SpringBootServletInitializer {
   	
	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ShopApplication.class);
    }

	@GetMapping
    public String hello(){
        return "Hey, this is success hello!";
    }
//    @GetMapping("/**")
//    public String handleAllRequests() {
//        return "Hey, this is success handleAllRequests()!";
//    }

	@GetMapping("/shop-0.0.1-SNAPSHOT/")
    public String shopSnapshot() {
      return "Hey, this is success shopSnapshot()!";
    }

	public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        SpringApplication.run(ShopApplication.class, args);
	}
	
}

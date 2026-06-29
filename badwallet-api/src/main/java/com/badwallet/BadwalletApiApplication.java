package com.badwallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BadwalletApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(BadwalletApiApplication.class, args);
    }
}

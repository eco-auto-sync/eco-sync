package com.ecosync.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.ecosync")
public class EcoSyncApiApplication {

    static void main(String[] args) {
        SpringApplication.run(EcoSyncApiApplication.class, args);
    }

}

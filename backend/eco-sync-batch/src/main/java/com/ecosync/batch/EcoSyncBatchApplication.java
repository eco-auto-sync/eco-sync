package com.ecosync.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.ecosync")
@EnableScheduling
public class EcoSyncBatchApplication {

    static void main(String[] args) {
        SpringApplication.run(EcoSyncBatchApplication.class, args);
    }

}

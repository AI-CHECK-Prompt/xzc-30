package com.carbon.carbon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CarbonTradingPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarbonTradingPlatformApplication.class, args);
    }
}

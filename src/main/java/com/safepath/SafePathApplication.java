package com.safepath;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SafePathApplication {

    public static void main(String[] args) {
        SpringApplication.run(SafePathApplication.class, args);
    }

}

package com.umc.hwaroak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class HwaroakApplication {

    public static void main(String[] args) {
        SpringApplication.run(HwaroakApplication.class, args);
    }

}

package com.via.ems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.via.ems"})
public class EmsApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmsApiApplication.class, args);
    }
}

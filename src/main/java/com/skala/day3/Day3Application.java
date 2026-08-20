package com.skala.day3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Day3Application {

    public static void main(String[] args) {
        SpringApplication.run(Day3Application.class, args);
    }
}

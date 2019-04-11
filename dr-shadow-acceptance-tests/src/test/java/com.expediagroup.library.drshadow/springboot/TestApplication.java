package com.expediagroup.library.drshadow.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Set the logging level in application.properties to debug
 */
@SpringBootApplication(scanBasePackages = {"com.expediagroup.library.drshadow"})
public class TestApplication {
    public static void main(String[] args) {

        SpringApplication.run(TestApplication.class, args);
    }
}

package com.healthcare.childrecords;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.healthcare.childrecords.repository")
public class ChildHealthcareApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChildHealthcareApplication.class, args);
    }
}

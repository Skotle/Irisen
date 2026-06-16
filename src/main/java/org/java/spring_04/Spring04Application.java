package org.java.spring_04;

import org.java.spring_04.common.StartupInput;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class Spring04Application {
    public static void main(String[] args) {
        StartupInput.collectAndApply();
        SpringApplication.run(Spring04Application.class, args);
    }
}

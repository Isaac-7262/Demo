package org.kku;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KkuIncidentMapApplication {
    public static void main(String[] args) {
        SpringApplication.run(KkuIncidentMapApplication.class, args);
    }
}

package com.manualjudicial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ManualJudicialApplication {
    public static void main(String[] args) {
        SpringApplication.run(ManualJudicialApplication.class, args);
    }
}

package com.mecanicaplus;

import com.mecanicaplus.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class MecanicaPlusApplication {

    public static void main(String[] args) {
        SpringApplication.run(MecanicaPlusApplication.class, args);
    }
}

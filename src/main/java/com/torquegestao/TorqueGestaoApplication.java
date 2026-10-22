package com.torquegestao;

import com.torquegestao.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class TorqueGestaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TorqueGestaoApplication.class, args);
    }
}

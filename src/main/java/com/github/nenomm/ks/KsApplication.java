package com.github.nenomm.ks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(CommonConfigProperties.class)
public class KsApplication {

    public static void main(String[] args) {
        SpringApplication.run(KsApplication.class, args);
    }

}

package com.example.csrf.attack.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.IdGenerator;
import org.springframework.util.JdkIdGenerator;

@Configuration
public class IdGeneratorConfiguration {

    @Bean
    IdGenerator idGenerator() {
        return new JdkIdGenerator();
    }
}

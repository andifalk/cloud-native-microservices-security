package com.example.csrf.attack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

@EnableJdbcHttpSession
@SpringBootApplication
public class CsrfAttackDemoApplication {

  public static void main(String[] args) {
    SpringApplication.run(CsrfAttackDemoApplication.class, args);
  }

}

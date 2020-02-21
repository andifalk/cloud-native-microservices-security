package com.example.csrf.attack.security;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests(authReq -> authReq.anyRequest().authenticated())
        //.csrf().disable()
        .formLogin(Customizer.withDefaults())
        .httpBasic(Customizer.withDefaults());
  }
}

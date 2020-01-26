package com.example.libraryserver.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

  @Primary
  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Qualifier("LegacyEncoder")
  @Bean
  public PasswordEncoder legacyPasswordEncoder() {
    String encodingId = "MD5";
    Map<String, PasswordEncoder> encoders = new HashMap<>();
    encoders.put(
        encodingId,
        new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("MD5"));
    return new DelegatingPasswordEncoder(encodingId, encoders);
  }

  @Configuration
  public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
      //http.csrf().disable();
      http.authorizeRequests(
              authorizeRequests ->
                  authorizeRequests
                      .requestMatchers(EndpointRequest.to(HealthEndpoint.class, InfoEndpoint.class))
                      .permitAll()
                      .requestMatchers(EndpointRequest.toAnyEndpoint())
                      .authenticated()
                      .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                      .permitAll()
                      .anyRequest()
                      .authenticated())
          .httpBasic(withDefaults())
          .formLogin(withDefaults());
    }
  }
}

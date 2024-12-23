package com.chatSDK.SupportSync;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SpringSecurity {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //authenticate the user before giving the access of api
        http.authorizeHttpRequests(
                auth->auth.requestMatchers("/health-checkup").permitAll().anyRequest().authenticated()
        );

        // if req not authenticated then show a popup
        http.httpBasic(withDefaults());
        // disable csrf token

        http.csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
}


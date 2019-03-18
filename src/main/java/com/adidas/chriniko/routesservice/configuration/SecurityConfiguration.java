package com.adidas.chriniko.routesservice.configuration;

import com.google.common.hash.Hashing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

@EnableWebFluxSecurity
@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf()
                .disable()
                .authorizeExchange()
                .anyExchange().authenticated()
                .and()
                .httpBasic().and()
                .formLogin();

        return http.build();
    }

    @Value("${security.username}")
    private String username;

    @Value("${security.password}")
    private String password;

    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        String hashedPassword = sha512(password);
        UserDetails user = new User(username, hashedPassword, Collections.singletonList(new SimpleGrantedAuthority("USER")));

        return new MapReactiveUserDetailsService(user);
    }

    @Bean
    DelegatingPasswordEncoder delegatingPasswordEncoder() {
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        DelegatingPasswordEncoder delegatingPasswordEncoder = (DelegatingPasswordEncoder) passwordEncoder;

        delegatingPasswordEncoder.setDefaultPasswordEncoderForMatches(new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return sha512(rawPassword);
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                String result = sha512(rawPassword);
                return result.equals(encodedPassword);
            }
        });

        return delegatingPasswordEncoder;
    }

    private String sha512(CharSequence rawPassword) {
        return Hashing
                .sha512()
                .hashString(rawPassword, StandardCharsets.UTF_8)
                .toString();
    }

}

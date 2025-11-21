package com.rmh.auth.config;

import com.rmh.auth.security.JwtAuthenticationFilter;
import com.rmh.auth.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtFilter;
    private final boolean swaggerEnabled;

    private static final String[] SWAGGER_WHITELIST = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**"
    };

    public SecurityConfig(
        CustomUserDetailsService uds,
        JwtAuthenticationFilter jf,
        @Value("${springdoc.swagger-ui.enabled:false}") boolean swaggerEnabled
    ){
        this.userDetailsService = uds;
        this.jwtFilter = jf;
        this.swaggerEnabled = swaggerEnabled;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                if (swaggerEnabled) {
                    auth.requestMatchers(SWAGGER_WHITELIST).permitAll();
                }
                auth
                    .requestMatchers("/api/auth/**", "/actuator/health/**").permitAll()
                    .anyRequest().authenticated();
            })
            .authenticationProvider(daoAuthProvider())
            .addFilterBefore(jwtFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthProvider(){
        DaoAuthenticationProvider p = new DaoAuthenticationProvider(userDetailsService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}

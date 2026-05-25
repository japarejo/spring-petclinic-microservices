package org.springframework.samples.microservicechasis.configuration;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.samples.microservicechasis.util.JwtRequestValidationFilter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author japarejo
 */
@Configuration
@EnableWebSecurity            // opcional en Boot ≥ 3, pero se mantiene por claridad
@EnableMethodSecurity         
public class SecurityConfiguration {

    //@Autowired
    //private JwtRequestValidationFilter filter;   // filtro JWT personalizado

    /** Reglas de autorización, filtros adicionales, CSRF, etc. */
    @Bean
    SecurityFilterChain filterChain(
            org.springframework.security.config.annotation.web.builders.HttpSecurity http)
            throws Exception {

        http
            /*---------------------------  AUTORIZAR PETICIONES  ---------------------------*/
            .authorizeHttpRequests(auth -> auth
                /* rutas completamente públicas -------------------------------------------*/
                .requestMatchers(
                    "/authenticate",
                    "/",
                    "/doc",
                    "/doc/swagger-config",
                    "/swagger*",
                    "/swagger-ui/**"
                ).permitAll()

                /* API REST expuesta públicamente -----------------------------------------*/
                .requestMatchers("/api/v1/bills/whoami/**","/env").permitAll()
                .requestMatchers("/api/**").permitAll()

                /* rutas que exigen autenticación ------------------------------------------*/
                .requestMatchers("/service-instances/*").permitAll()
                .requestMatchers("/actuator/**").permitAll()

                /* cualquier otra URL se deniega ------------------------------------------*/
                .anyRequest().permitAll()
            )

            /*---------------------  SESIÓN: STATELESS (JWT)  -----------------------------*/
            .sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            /*----------------  INSERTAR FILTRO JWT ANTES DE AUTH FILTER  -----------------*/
            //.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)

            /*--------------------------  CSRF DESHABILITADO  -----------------------------*/
            .csrf(csrf -> csrf.disable());

        return http.build();
    }

    /** Codificador de contraseñas (solo para demo / entorno controlado) */
    @Bean
    PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}



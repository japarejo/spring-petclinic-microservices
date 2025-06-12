package org.springframework.samples.microservicechasis.configuration;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.samples.microservicechasis.util.JwtRequestValidationFilter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author japarejo
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
	
	@Autowired
	JwtRequestValidationFilter filter;
	@Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {		

        http
            /*------------------------------------------------------------------
             * 1. AUTORIZACIÓN DE PETICIONES
             *-----------------------------------------------------------------*/
            .authorizeHttpRequests(auth -> auth
                /*---- áreas públicas ---------------------------------------------------*/
                .requestMatchers(
                    "/authenticate",
                    "/",
                    "/doc",
                    "/doc/swagger-config",
                    "/swagger*",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-ui.html"
                ).permitAll()

                /*---- áreas protegidas -------------------------------------------------*/
                .requestMatchers("/api/**").authenticated()
                .requestMatchers("/service-instances/*").authenticated()
                .requestMatchers("/actuator/**").authenticated()

                /*---- cualquier otra ruta, denegada -----------------------------------*/
                .anyRequest().denyAll()
            )

            /*------------------------------------------------------------------
             * 2. POLÍTICA DE SESIÓN: STATELESS
             *-----------------------------------------------------------------*/
            .sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            /*------------------------------------------------------------------
             * 3. CADENA DE FILTROS: insertar «filter» antes de UsernamePasswordAuthenticationFilter
             *-----------------------------------------------------------------*/
            .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)

            /*------------------------------------------------------------------
             * 4. MECANISMOS DE AUTENTICACIÓN Y OTRAS CONFIGS
             *-----------------------------------------------------------------*/
            .httpBasic(            c -> {})   // ← active sólo si realmente lo usa
            //.formLogin(          c -> {})   // ← comentar si es API REST pura
            .csrf(csrf -> csrf.disable());    // para APIs sin cookie-based login

        /* El método build() genera el SecurityFilterChain que Spring Boot inyectará */
        return http.build();
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {	    
		PasswordEncoder encoder =  NoOpPasswordEncoder.getInstance();
	    return encoder;
	}
		
	
}



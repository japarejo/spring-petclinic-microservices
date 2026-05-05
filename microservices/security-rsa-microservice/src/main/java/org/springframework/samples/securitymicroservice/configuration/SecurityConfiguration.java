package org.springframework.samples.securitymicroservice.configuration;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.samples.securitymicroservice.util.JwtRequestFilter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
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
@EnableWebSecurity                // opcional (Boot lo activa), útil para claridad
@EnableMethodSecurity             
public class SecurityConfiguration {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JwtRequestFilter filter;            // filtro JWT propio

    /* ------------------------------------------------------------------
     * 1.  CADENA DE FILTROS Y REGLAS DE AUTORIZACIÓN
     * ---------------------------------------------------------------- */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // ---------- reglas de autorización ----------
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/authenticate",
                    "/validate",
                    "/refresh",
                    "/",
                    "/doc",
                    "/doc/swagger-config",
                    "/swagger*",
                    "/swagger-ui/**"
                ).permitAll()                                              // públicas
                .requestMatchers("/api/**").authenticated()
                .requestMatchers("/service-instances/*").authenticated()
                .requestMatchers("/actuator/**").authenticated()
                .anyRequest().denyAll()
            )

            // ---------- política de sesión ---------------
            .sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // ---------- filtro JWT antes del de usuario/contraseña ------
            .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)

            // ---------- deshabilitar CSRF para API token-based ----------
            .csrf(csrf -> csrf.disable());

        return http.build();
    }

    /* ------------------------------------------------------------------
     * 2.  USER-DETAILS SERVICE JDBC (equivale al bloque
     *     configure(AuthenticationManagerBuilder …) de la versión vieja)
     * ---------------------------------------------------------------- */
    @Bean
    UserDetailsService jdbcUserDetailsService(DataSource dataSource) {
        JdbcUserDetailsManager uds = new JdbcUserDetailsManager(dataSource);
        uds.setUsersByUsernameQuery(
            "select username, password, enabled from users where username = ?");
        uds.setAuthoritiesByUsernameQuery(
            "select username, authority from authorities where username = ?");
        return uds;
    }

    /* ------------------------------------------------------------------
     * 3.  PASSWORD ENCODER  (idéntico a la versión anterior)
     * ---------------------------------------------------------------- */
    @Bean
    PasswordEncoder passwordEncoder() {
        //  Sólo para escenarios formativos / demo.
        return NoOpPasswordEncoder.getInstance();
    }

    /* ------------------------------------------------------------------
     * 4.  AUTHENTICATION MANAGER  (sustituye a authenticationManagerBean())
     * ---------------------------------------------------------------- */
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration cfg)
            throws Exception {
        // Spring genera el AuthenticationManager combinando el UserDetailsService
        // anterior y el PasswordEncoder declarado.
        return cfg.getAuthenticationManager();
    }
}

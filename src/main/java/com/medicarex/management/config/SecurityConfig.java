package com.medicarex.management.config;

import com.medicarex.management.security.JwtAuthFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http

            .csrf(csrf -> csrf.disable())


            .cors(cors ->
                cors.configurationSource(
                    corsConfigurationSource()
                )
            )


            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )


            .authorizeHttpRequests(auth -> auth


                // CORS
                .requestMatchers(
                    HttpMethod.OPTIONS,
                    "/**"
                ).permitAll()


                // Login and registration
                .requestMatchers(
                    "/users/register",
                    "/users/login"
                ).permitAll()


                // =========================
                // DOCTORS
                // =========================

                // Patients can view doctors
                .requestMatchers(
    HttpMethod.GET,
    "/doctors",
    "/doctors/**"
).hasAnyRole(
    "PATIENT",
    "DOCTOR",
    "ADMIN"
)

                // Only admin can add/update/delete doctors
                .requestMatchers(
                    HttpMethod.POST,
                    "/doctors"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.PUT,
                    "/doctors/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/doctors/**"
                ).hasRole("ADMIN")


                // =========================
                // APPOINTMENTS
                // =========================

                .requestMatchers(
                    HttpMethod.POST,
                    "/appointments"
                ).hasRole("PATIENT")


                .requestMatchers(
                    HttpMethod.GET,
                    "/appointments"
                ).hasRole("ADMIN")


                .requestMatchers(
                    "/appointments/patient/**"
                ).hasRole("PATIENT")


                .requestMatchers(
                    "/appointments/doctor/**"
                ).hasRole("DOCTOR")


                .requestMatchers(
                    "/appointments/*/cancel"
                ).hasRole("PATIENT")


                .requestMatchers(
                    "/appointments/*/status"
                ).hasAnyRole(
                    "DOCTOR",
                    "ADMIN"
                )


                .requestMatchers(
                    "/appointments/**"
                ).hasRole("ADMIN")


                // =========================
                // PATIENT
                // =========================

                .requestMatchers(
                    "/patient/**"
                ).hasRole("PATIENT")


                // =========================
                // DOCTOR
                // =========================

                .requestMatchers(
                    "/doctor/**"
                ).hasRole("DOCTOR")


                // =========================
                // ADMIN
                // =========================

                .requestMatchers(
                    "/admin/**"
                ).hasRole("ADMIN")


                // =========================
                // USERS
                // =========================

                .requestMatchers(
                    HttpMethod.GET,
                    "/users"
                ).hasRole("ADMIN")


                .requestMatchers(
                    HttpMethod.DELETE,
                    "/users/*"
                ).hasRole("ADMIN")


                // Everything else
                .anyRequest()
                .authenticated()
            )


            .addFilterBefore(
                jwtAuthFilter,
                UsernamePasswordAuthenticationFilter.class
            )


            .formLogin(form ->
                form.disable()
            )


            .httpBasic(basic ->
                basic.disable()
            );


        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();


        configuration.setAllowedOrigins(
            List.of(
                "http://localhost:5173"
            )
        );


        configuration.setAllowedMethods(
            List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
            )
        );


        configuration.setAllowedHeaders(
            List.of(
                "Authorization",
                "Content-Type"
            )
        );


        configuration.setAllowCredentials(true);


        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();


        source.registerCorsConfiguration(
            "/**",
            configuration
        );


        return source;
    }
}
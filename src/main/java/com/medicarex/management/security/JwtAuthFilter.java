package com.medicarex.management.security;

import com.medicarex.management.entity.User;
import com.medicarex.management.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthFilter(
            JwtService jwtService,
            UserRepository userRepository) {

        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader =
                request.getHeader("Authorization");


        /*
         * No JWT token
         */

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        /*
         * Extract token
         */

        String token =
                authHeader.substring(7);


        try {

            /*
             * Get email from JWT
             */

            String email =
                    jwtService.extractEmail(token);


            /*
             * Find user from database
             */

            User user =
                    userRepository
                            .findByEmail(email)
                            .orElse(null);


            if (user != null &&
                    user.getRole() != null) {


                /*
                 * Create Spring Security role
                 *
                 * Example:
                 *
                 * ADMIN  -> ROLE_ADMIN
                 * PATIENT -> ROLE_PATIENT
                 * DOCTOR -> ROLE_DOCTOR
                 */

                String role =
                        user.getRole()
                                .trim()
                                .toUpperCase();


                SimpleGrantedAuthority authority =
                        new SimpleGrantedAuthority(
                                "ROLE_" + role
                        );


                /*
                 * Debug information
                 */

                System.out.println(
                        "===================================="
                );

                System.out.println(
                        "JWT EMAIL: " + email
                );

                System.out.println(
                        "USER FROM DB: " +
                        user.getEmail()
                );

                System.out.println(
                        "USER ROLE FROM DB: [" +
                        user.getRole() +
                        "]"
                );

                System.out.println(
                        "AUTHORITY CREATED: [" +
                        authority.getAuthority() +
                        "]"
                );


                /*
                 * Create authentication
                 */

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                user.getEmail(),
                                null,
                                List.of(authority)
                        );


                /*
                 * Put authentication into
                 * Spring Security context
                 */

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(
                                authentication
                        );


                System.out.println(
                        "AUTHENTICATED: " +
                        SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .isAuthenticated()
                );


                System.out.println(
                        "AUTHORITIES: " +
                        SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getAuthorities()
                );

                System.out.println(
                        "===================================="
                );
            }


        } catch (Exception e) {

            /*
             * Invalid or expired JWT.
             *
             * Clear any authentication so that
             * Spring Security handles the request
             * normally.
             */

            SecurityContextHolder
                    .clearContext();


            System.out.println(
                    "JWT AUTHENTICATION FAILED: " +
                    e.getMessage()
            );
        }


        /*
         * Continue request
         */

        filterChain.doFilter(
                request,
                response
        );
    }
}
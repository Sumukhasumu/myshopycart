
package com.myshopycart.productservice.security;

import jakarta.servlet.DispatcherType;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtService jwtService) {
        return new JwtAuthenticationFilter(jwtService);
    }

    /*
     * Register the JWT filter inside Spring Security only.
     * Prevent duplicate registration as a general servlet filter.
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter>
    jwtAuthenticationFilterRegistration(
            JwtAuthenticationFilter filter) {

        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                new FilterRegistrationBean<>(filter);

        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers("/actuator/health").permitAll()

                // Only admins can create, update, or delete categories.
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/products/categories"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/products/categories/*"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/products/categories/*"
                ).hasRole("ADMIN")

                // Distributor/admin product listing.
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/products/my-products"
                ).hasAnyRole("DISTRIBUTOR", "ADMIN")

                // Public product and category browsing.
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/products/**"
                ).permitAll()

                // Product creation and image upload.
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/products",
                    "/api/products/*/images"
                ).hasAnyRole("DISTRIBUTOR", "ADMIN")

                // Product updates.
                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/products/*"
                ).hasAnyRole("DISTRIBUTOR", "ADMIN")

                // Product deletion and image deletion.
                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/products/*",
                    "/api/products/*/images"
                ).hasAnyRole("DISTRIBUTOR", "ADMIN")

                // Other product endpoints require authentication.
                .requestMatchers("/api/products/**").authenticated()

                .anyRequest().authenticated()
            )
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
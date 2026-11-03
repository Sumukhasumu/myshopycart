
package com.myshopycart.inventoryservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()

                        .requestMatchers("/actuator/health")
                        .permitAll()

                        // Distributor inventory listing
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/inventory/my-inventory"
                        ).hasAnyRole("ADMIN", "DISTRIBUTOR")

                        // Stock availability
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/inventory/*/availability"
                        ).hasAnyRole(
                                "ADMIN",
                                "DISTRIBUTOR",
                                "INVENTORY_SERVICE"
                        )

                        // Reservation lifecycle
                        .requestMatchers(
                                "/api/inventory/*/reserve",
                                "/api/inventory/*/release",
                                "/api/inventory/*/commit"
                        ).hasAnyRole("ADMIN", "INVENTORY_SERVICE")

                        // Inventory creation and stock additions
                        .requestMatchers(
        			HttpMethod.POST,
        			"/api/inventory",
        			"/api/inventory/*/stock",
        			"/api/inventory/*/adjust"
			).hasAnyRole("ADMIN", "DISTRIBUTOR")

			// Inventory transaction history
			.requestMatchers(
        		    	HttpMethod.GET,
        		    	"/api/inventory/*/history"
			).hasAnyRole("ADMIN", "DISTRIBUTOR")

                        // Inventory reads
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/inventory/*"
                        ).hasAnyRole("ADMIN", "DISTRIBUTOR")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
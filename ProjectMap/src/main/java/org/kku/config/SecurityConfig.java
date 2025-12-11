package org.kku.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Public assets and landing pages
                .requestMatchers("/", "/webjars/**", "/css/**", "/js/**", "/uploads/**").permitAll()
                // Public incident submission and read-only APIs
                .requestMatchers(
                    "/api/incidents", "/api/incidents/active", "/api/incidents/stats",
                    "/api/incidents/type/**", "/api/incidents/stream", "/api/incidents/submit"
                ).permitAll()
                // Officer/Admin dashboards and management
                .requestMatchers("/dashboard", "/api/admin/**", "/api/officer/**").hasAnyRole("OFFICER", "ADMIN")
                // User management endpoints are ADMIN only for now
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                // Everything else: allow, controllers can recheck via token/role for sensitive ops
                .anyRequest().permitAll()
            )
            .httpBasic(Customizer.withDefaults())
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public UserDetailsService users() {
        InMemoryUserDetailsManager uds = new InMemoryUserDetailsManager();
        // Reporter: can submit incidents and view public info
        uds.createUser(User.withUsername("reporter")
            .password("{noop}kku1234")
            .roles("REPORTER")
            .build());

        // Officer: can manage incidents and chat
        uds.createUser(User.withUsername("officer")
            .password("{noop}kku1234")
            .roles("OFFICER")
            .build());

        // Admin: can manage users and system settings
        uds.createUser(User.withUsername("admin")
            .password("{noop}kkuAdmin!1234")
            .roles("ADMIN")
            .build());
        return uds;
    }
}

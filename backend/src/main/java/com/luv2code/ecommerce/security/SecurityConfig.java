package com.luv2code.ecommerce.security;

import com.luv2code.ecommerce.dao.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final String allowedOrigins;

    public SecurityConfig(JwtService jwtService, UserRepository userRepository,
                          @Value("${app.allowed-origins}") String allowedOrigins) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // stateless REST API with a token in the header: no session, no CSRF cookie
            .csrf().disable()
            .cors().and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .authorizeRequests(auth -> auth
                // public catalog
                .antMatchers(HttpMethod.GET, "/api/products/**", "/api/product-category/**",
                        "/api/countries/**", "/api/states/**").permitAll()
                // sign up and log in
                .antMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                // guests can still place an order
                .antMatchers(HttpMethod.POST, "/api/checkout/purchase").permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // admin area
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                // everything else (my account, my orders...) needs a logged-in user
                .anyRequest().authenticated())
            // 401 instead of a login page when the token is missing or invalid
            .exceptionHandling().authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)).and()
            .addFilterBefore(new JwtAuthenticationFilter(jwtService, userRepository),
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}

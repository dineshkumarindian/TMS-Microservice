package com.servxglobal.tms.userservice.security;

import com.servxglobal.tms.userservice.model.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
//@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthEntryPoint jwtAuthEntryPoint;

    /**
     * Configures the security filter chain for the HTTP requests.
     *
     * @param http the HttpSecurity object
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers(CorsUtils::isPreFlightRequest).permitAll() // Allow preflight request
                                .requestMatchers("/api/public/**").hasAuthority(UserType.TRAINER.toString())
                                .requestMatchers("/api/user/admin/register").permitAll()
                                .requestMatchers("/api/user/login/**").permitAll()
                                .requestMatchers("/api/user/trainee/register").hasAuthority(UserType.ADMIN.toString())
                                .requestMatchers("/api/user/trainer/register").hasAuthority(UserType.ADMIN.toString())
                                .requestMatchers("/api/user/admin/**").hasAuthority(UserType.ADMIN.toString())
                                .requestMatchers("/api/user/courses/**").hasAuthority(UserType.ADMIN.toString())
                                .requestMatchers("/api/course/**").hasAuthority(UserType.ADMIN.toString())
                                .requestMatchers("/api/stream/**").hasAuthority(UserType.ADMIN.toString())

                                .anyRequest().authenticated());
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        http.cors(); // Enable CORS
        return http.build();
    }

    /**
     * Creates an instance of AuthenticationManager using the provided AuthenticationConfiguration.
     *
     * @param authenticationConfiguration The AuthenticationConfiguration object.
     * @return The created AuthenticationManager.
     * @throws Exception If an error occurs while creating the AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Returns a BCryptPasswordEncoder instance.
     * @return the BCryptPasswordEncoder instance
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    /**
     * Configures the CORS (Cross-Origin Resource Sharing) for the application.
     *
     * @return The configured CorsConfigurationSource.
     */
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        // Create a new CorsConfiguration object
//        CorsConfiguration corsConfiguration = new CorsConfiguration();
//
//        // Set the allowed origins
//        corsConfiguration.setAllowedOrigins(
//                List.of("http://localhost:4200", "https://tms.sxgcloud.com/")); // Replace with your allowed origins
//
//        // Set the allowed methods
//        corsConfiguration.setAllowedMethods(
//                List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Specify allowed HTTP methods
//
//        // Set the allowed headers
//        corsConfiguration.setAllowedHeaders(
//                List.of("*")); // Allow all headers (customize as needed)
//
//        // Enable credentials (cookies)
//        corsConfiguration.setAllowCredentials(true);
//
//        // Create a new UrlBasedCorsConfigurationSource object
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//
//        // Register the CorsConfiguration to be applied to all paths
//        source.registerCorsConfiguration("/**", corsConfiguration);
//
//        // Return the configured CorsConfigurationSource
//        return source;
//    }
}

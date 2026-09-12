package org.sid.restaurationbackend.security.config;

import lombok.AllArgsConstructor;

import org.sid.restaurationbackend.security.jwt.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;


    // =========================================================
    // PASSWORD ENCODER
    // =========================================================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    // =========================================================
    // AUTHENTICATION MANAGER
    // =========================================================

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }


    // =========================================================
    // SECURITY FILTER CHAIN
    // =========================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http)
            throws Exception {

        http

                // =================================================
                // CORS
                // =================================================

                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )


                // =================================================
                // CSRF
                // =================================================

                .csrf(csrf ->
                        csrf.disable()
                )


                // =================================================
                // SESSION
                // =================================================

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )


                // =================================================
                // AUTHORIZATION
                // =================================================

                .authorizeHttpRequests(auth -> auth

                        // =========================================
                        // IMPORTANT :
                        // AUTORISER LES REQUÊTES PREFLIGHT
                        // =========================================

                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()


                        // =========================================
                        // AUTHENTIFICATION
                        // =========================================

                        .requestMatchers(
                                "/api/auth/**"
                        ).permitAll()


                        // =========================================
                        // GESTION DES ROLES / PERMISSIONS
                        // =========================================
                        // Lecture nécessaire au Back Office (sidebar,
                        // formulaire de rôle, etc.) : réservée aux
                        // utilisateurs authentifiés côté Back Office.
                        // Écriture (créer/modifier/supprimer un rôle,
                        // une fonctionnalité, une association rôle-
                        // fonctionnalité) : réservée à l'administrateur.

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/fonctionnalites/**",
                                "/api/roles/**",
                                "/api/roleFonctionnalites/**",
                                "/api/modules-fonctionnalites/**"
                        ).hasAuthority("INTERFACE_BACKOFFICE")

                        .requestMatchers(
                                "/api/fonctionnalites/**",
                                "/api/roles/**",
                                "/api/roleFonctionnalites/**",
                                "/api/modules-fonctionnalites/**"
                        ).hasAuthority("ROLE_SUPERADMIN")


                        // =========================================
                        // EMPLOYES
                        // =========================================
                        // Lecture : Back Office authentifié.
                        // Écriture (créer/modifier/supprimer un
                        // employé) : réservée à l'administrateur.

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/employees/**"
                        ).hasAuthority("INTERFACE_BACKOFFICE")

                        .requestMatchers(
                                "/api/employees/**"
                        ).hasAuthority("ROLE_SUPERADMIN")

                        // =========================================
                        // MON COMPTE
                        // =========================================
                        // Auto-modification du compte connecté (voir
                        // MonCompteController) : accessible à tout
                        // utilisateur authentifié (Employee, SuperAdmin,
                        // ClientAuthentifie), sans distinction de rôle —
                        // couverte par la règle générale .anyRequest()
                        // .authenticated() plus bas, donc aucun matcher
                        // dédié n'est nécessaire ici. Ne pas ajouter
                        // "/api/mon-compte/**" sous "/api/employees/**"
                        // ni "ROLE_SUPERADMIN" par erreur.


                        // =========================================
                        // PUBLIC
                        // =========================================

                        .requestMatchers(
                                "/",
                                "/error",
                                "/favicon.ico"
                        ).permitAll()


                        // =========================================
                        // FICHIERS UPLOADÉS (photos de profil, etc.)
                        // =========================================
                        // Lecture publique nécessaire : une balise
                        // <img src="..."> ne peut pas envoyer le
                        // header Authorization. Voir FileStorageService
                        // et WebConfig.

                        .requestMatchers(
                                HttpMethod.GET,
                                "/uploads/**"
                        ).permitAll()


                        // =========================================
                        // IMAGES STATIQUES (catégories / produits)
                        // =========================================
                        // Servies automatiquement par Spring Boot depuis
                        // src/main/resources/static/images/** (aucune
                        // config resource handler nécessaire, contrairement
                        // à /uploads/**). Même raison que ci-dessus : une
                        // balise <img> ne peut pas s'authentifier.

                        .requestMatchers(
                                HttpMethod.GET,
                                "/images/**"
                        ).permitAll()


                        // =========================================
                        // SWAGGER
                        // =========================================

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()


                        // =========================================
                        // CLIENT
                        // =========================================

                        .requestMatchers(
                                "/api/client/**"
                        ).hasAuthority(
                                "INTERFACE_CLIENT"
                        )


                        // =========================================
                        // PUBLIC API
                        // =========================================

                        .requestMatchers(
                                "/api/public/**"
                        ).permitAll()


                        // =========================================
                        // PDV
                        // =========================================

                        .requestMatchers(
                                "/api/pdv/**"
                        ).hasAuthority(
                                "INTERFACE_PDV"
                        )


                        // =========================================
                        // BACK OFFICE
                        // =========================================

                        .requestMatchers(
                                "/api/back-office/**"
                        ).hasAuthority(
                                "INTERFACE_BACKOFFICE"
                        )


                        // =========================================
                        // ADMIN
                        // =========================================

                        .requestMatchers(
                                "/api/admin/**"
                        ).hasAuthority(
                                "ROLE_SUPERADMIN"
                        )


                        // =========================================
                        // RESTE
                        // =========================================

                        .anyRequest().authenticated()
                )


                // =================================================
                // JWT FILTER
                // =================================================

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );


        return http.build();
    }


    // =========================================================
    // CORS CONFIGURATION
    // =========================================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();


        // =========================================================
        // ANGULAR
        // =========================================================

        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:4200",
                        "http://localhost:3000"
                )
        );


        // =========================================================
        // METHODES
        // =========================================================

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "PATCH",
                        "OPTIONS"
                )
        );


        // =========================================================
        // HEADERS
        // =========================================================

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept",
                        "Origin",
                        "X-Requested-With"
                )
        );


        // =========================================================
        // HEADERS EXPOSED
        // =========================================================

        configuration.setExposedHeaders(
                List.of(
                        "Authorization"
                )
        );


        // =========================================================
        // CREDENTIALS
        // =========================================================

        configuration.setAllowCredentials(true);


        // =========================================================
        // REGISTRATION
        // =========================================================

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );


        return source;
    }
}
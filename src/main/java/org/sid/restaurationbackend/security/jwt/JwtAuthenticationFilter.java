package org.sid.restaurationbackend.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // =========================================================
        // OPTIONS / PREFLIGHT
        // =========================================================

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // =========================================================
        // AUTHORIZATION HEADER
        // =========================================================

        String authorizationHeader =
                request.getHeader("Authorization");

        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        // =========================================================
        // TOKEN
        // =========================================================

        String token =
                authorizationHeader.substring(7).trim();

        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {

            // =====================================================
            // VALIDATION TOKEN
            // =====================================================

            if (!jwtUtil.isTokenValid(token)) {

                SecurityContextHolder.clearContext();

                filterChain.doFilter(request, response);
                return;
            }

            // =====================================================
            // EVITER DOUBLE AUTHENTIFICATION
            // =====================================================

            if (SecurityContextHolder
                    .getContext()
                    .getAuthentication() == null) {

                // =================================================
                // USERNAME / EMAIL
                // =================================================

                String email =
                        jwtUtil.extractUsername(token);

                // =================================================
                // ROLE
                // =================================================

                String role =
                        jwtUtil.extractRole(token);

                // =================================================
                // INTERFACE
                // =================================================

                String interfaceType = null;

                if (jwtUtil.extractInterfaceType(token) != null) {

                    interfaceType =
                            jwtUtil
                                    .extractInterfaceType(token)
                                    .name();
                }

                // =================================================
                // PERMISSIONS
                // =================================================

                List<String> permissions =
                        jwtUtil.extractPermissions(token);

                // =================================================
                // AUTHORITIES
                // =================================================

                List<SimpleGrantedAuthority> authorities =
                        new ArrayList<>();

                // -------------------------------------------------
                // ROLE
                // -------------------------------------------------

                if (role != null &&
                        !role.isBlank()) {

                    authorities.add(
                            new SimpleGrantedAuthority(
                                    "ROLE_" +
                                            role.toUpperCase()
                            ));
                }

                // -------------------------------------------------
                // PERMISSIONS
                // -------------------------------------------------

                if (permissions != null) {

                    permissions.stream()

                            .filter(permission ->
                                    permission != null &&
                                            !permission.isBlank()
                            )

                            .map(String::trim)

                            .map(String::toUpperCase)

                            .distinct()

                            .map(SimpleGrantedAuthority::new)

                            .forEach(authorities::add);
                }

                // -------------------------------------------------
                // INTERFACE
                // -------------------------------------------------

                if (interfaceType != null &&
                        !interfaceType.isBlank()) {

                    authorities.add(
                            new SimpleGrantedAuthority(
                                    "INTERFACE_" +
                                            interfaceType.toUpperCase()
                            ));
                }

                // =================================================
                // AUTHENTICATION
                // =================================================

                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                authorities
                        );

                // =================================================
                // DETAILS
                // =================================================

                ((UsernamePasswordAuthenticationToken)
                        authentication)
                        .setDetails(
                                new WebAuthenticationDetailsSource()
                                        .buildDetails(request)
                        );

                // =================================================
                // SECURITY CONTEXT
                // =================================================

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);
            }

        } catch (Exception e) {

            // -----------------------------------------------------
            // TOKEN INVALIDE
            // -----------------------------------------------------

            SecurityContextHolder.clearContext();

            System.err.println(
                    "Erreur JWT : " +
                            e.getMessage()
            );
        }

        // =========================================================
        // CONTINUER LA REQUETE
        // =========================================================

        filterChain.doFilter(request, response);
    }
}
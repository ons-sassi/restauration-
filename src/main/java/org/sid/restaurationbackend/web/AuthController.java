package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;

import org.sid.restaurationbackend.dtos.AuthResponseDTO;
import org.sid.restaurationbackend.dtos.BackOfficeLoginDTO;
import org.sid.restaurationbackend.dtos.ClientLoginDTO;
import org.sid.restaurationbackend.dtos.ClientRegisterDTO;
import org.sid.restaurationbackend.dtos.PdvLoginDTO;
import org.sid.restaurationbackend.dtos.LoginDTO;
import org.sid.restaurationbackend.dtos.SuperAdminLoginDTO;

import org.sid.restaurationbackend.exceptions.PointDeVenteNotFoundException;
import org.sid.restaurationbackend.exceptions.RestaurantNotFoundException;
import org.sid.restaurationbackend.services.AuthService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint unique pour Employee, Client et SuperAdmin.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginDTO loginDTO) {
        return ResponseEntity.ok(authService.login(loginDTO));
    }


    @PostMapping("/login/back-office")
    public ResponseEntity<AuthResponseDTO> loginBackOffice(
            @RequestBody BackOfficeLoginDTO loginDTO) {

        return ResponseEntity.ok(
                authService.loginBackOffice(loginDTO)
        );
    }


    @PostMapping("/login/pdv")
    public ResponseEntity<AuthResponseDTO> loginPdv(
            @RequestBody PdvLoginDTO loginDTO)
            throws PointDeVenteNotFoundException {

        return ResponseEntity.ok(
                authService.loginPdv(loginDTO)
        );
    }


    @PostMapping("/login/client")
    public ResponseEntity<AuthResponseDTO> loginClient(
            @RequestBody ClientLoginDTO loginDTO) {

        return ResponseEntity.ok(
                authService.loginClient(loginDTO)
        );
    }


    /**
     * Auto-inscription client — endpoint public (voir SecurityConfig,
     * "/api/auth/**" est déjà permitAll, aucune modification requise
     * côté sécurité).
     */
    @PostMapping("/register/client")
    public ResponseEntity<AuthResponseDTO> registerClient(
            @RequestBody ClientRegisterDTO registerDTO)
            throws RestaurantNotFoundException {

        return ResponseEntity
                .status(org.springframework.http.HttpStatus.CREATED)
                .body(
                        authService.registerClient(registerDTO)
                );
    }


    @PostMapping("/login/super-admin")
    public ResponseEntity<AuthResponseDTO> loginSuperAdmin(
            @RequestBody SuperAdminLoginDTO loginDTO) {

        return ResponseEntity.ok(
                authService.loginSuperAdmin(loginDTO)
        );
    }


    /**
     * Sélection du restaurant par le SUPERADMIN.
     * Le rôle reste SUPERADMIN ; seul le restaurant courant change.
     */
    @PostMapping("/select-restaurant/{restaurantId}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<AuthResponseDTO> selectRestaurant(
            @PathVariable Long restaurantId)
            throws RestaurantNotFoundException {

        return ResponseEntity.ok(
                authService.selectRestaurant(restaurantId)
        );
    }
}
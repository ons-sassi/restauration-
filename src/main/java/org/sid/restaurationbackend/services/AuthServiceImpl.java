package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.sid.restaurationbackend.dtos.AuthResponseDTO;
import org.sid.restaurationbackend.dtos.BackOfficeLoginDTO;
import org.sid.restaurationbackend.dtos.ClientLoginDTO;
import org.sid.restaurationbackend.dtos.ClientRegisterDTO;
import org.sid.restaurationbackend.dtos.LoginDTO;
import org.sid.restaurationbackend.dtos.PdvLoginDTO;
import org.sid.restaurationbackend.dtos.SuperAdminLoginDTO;

import org.sid.restaurationbackend.entities.ClientAuthentifie;
import org.sid.restaurationbackend.entities.Employee;
import org.sid.restaurationbackend.entities.PointDeVente;
import org.sid.restaurationbackend.entities.Restaurant;
import org.sid.restaurationbackend.entities.Role;
import org.sid.restaurationbackend.entities.RoleFonctionnalite;
import org.sid.restaurationbackend.entities.SuperAdmin;

import org.sid.restaurationbackend.enums.InterfaceType;
import org.sid.restaurationbackend.enums.StatutUtilisateur;

import org.sid.restaurationbackend.exceptions.PointDeVenteNotFoundException;
import org.sid.restaurationbackend.exceptions.InvalidCredentialsException;
import org.sid.restaurationbackend.exceptions.AccountInactiveException;
import org.sid.restaurationbackend.exceptions.EmailEnUtilisationException;
import org.sid.restaurationbackend.exceptions.RestaurantNotFoundException;
import org.sid.restaurationbackend.mappers.RestaurantMapper;

import org.sid.restaurationbackend.repositories.ClientAuthentifieRepository;
import org.sid.restaurationbackend.repositories.EmployeeRepository;
import org.sid.restaurationbackend.repositories.FonctionnaliteRepository;
import org.sid.restaurationbackend.repositories.RestaurantRepository;
import org.sid.restaurationbackend.repositories.RoleFonctionnaliteRepository;
import org.sid.restaurationbackend.repositories.SuperAdminRepository;

import org.sid.restaurationbackend.security.jwt.JwtUtil;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final EmployeeRepository employeeRepository;

    private final ClientAuthentifieRepository clientAuthentifieRepository;

    private final SuperAdminRepository superAdminRepository;

    private final RestaurantRepository restaurantRepository;

    private final RoleFonctionnaliteRepository roleFonctionnaliteRepository;

    private final FonctionnaliteRepository fonctionnaliteRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    private final PointDeVenteService pointDeVenteService;

    private final RestaurantMapper dtoMapper;


    // =========================================================
    // LOGIN UNIVERSEL
    // =========================================================

    @Override
    public AuthResponseDTO login(LoginDTO request) {

        if (request == null || request.getEmail() == null || request.getEmail().isBlank()
                || request.getMot_de_passe() == null || request.getMot_de_passe().isBlank()) {
            throw new IllegalArgumentException("Email et mot de passe sont obligatoires");
        }

        String email = request.getEmail().trim().toLowerCase();
        String password = request.getMot_de_passe();

        // 1) Employee : on vérifie le mot de passe avant de passer au type suivant.
        // Cela permet même un email identique dans plusieurs tables, avec le bon
        // mot de passe associé au compte choisi.
        var employeeOpt = employeeRepository.findByEmail(email);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();

            if (employee.getStatut() == null || employee.getStatut() == StatutUtilisateur.ACTIF) {
                if (employee.getMot_de_passe() != null
                        && passwordEncoder.matches(password, employee.getMot_de_passe())) {

                    Role role = employee.getRole();
                    if (role == null) {
                        throw new AccessDeniedException("Aucun rôle n'est associé à cet employé");
                    }

                    // Ce endpoint (login universel) n'est appelé que depuis la page
                    // "Connexion" (back office / super admin) du front, jamais depuis
                    // la page PDV (qui utilise loginPdv() avec un code PIN). Un employé
                    // dont le rôle n'a pas l'accès Back Office doit donc être rejeté
                    // ici avec un message clair, plutôt que d'être basculé
                    // silencieusement vers l'interface PDV : ce basculement silencieux
                    // était source de confusion (l'utilisateur se croyait connecté au
                    // Back Office alors qu'il atterrissait sur /pdv).
                    if (!Boolean.TRUE.equals(role.getAcces_backoffice())) {
                        throw new AccessDeniedException(
                                "Vous n'avez pas accès au Back Office. "
                                        + "Utilisez la connexion Point de Vente."
                        );
                    }

                    InterfaceType interfaceType = InterfaceType.BACKOFFICE;

                    List<String> permissions = getPermissions(role, interfaceType);

                    Long restaurantId = employee.getRestaurant() != null
                            ? employee.getRestaurant().getId_restaurant()
                            : null;
                    Long pdvId = employee.getPdvAffecte() != null
                            ? employee.getPdvAffecte().getId_pdv()
                            : null;

                    employee.setDate_derniere_connection(new Date());
                    employeeRepository.save(employee);

                    String token = jwtUtil.generateToken(
                            employee.getId_utilisateur(),
                            employee.getEmail(),
                            role.getNom_role(),
                            interfaceType,
                            permissions
                    );

                    log.info("Connexion universelle Employee réussie : employeeId={}, email={}, role={}, interface={}",
                            employee.getId_utilisateur(), email, role.getNom_role(), interfaceType);

                    return new AuthResponseDTO(
                            token, "Bearer", employee.getId_utilisateur(), employee.getEmail(),
                            role.getNom_role(), interfaceType, pdvId, restaurantId,
                            permissions, false, null
                    );
                }
            } else {
                throw new AccountInactiveException("Ce compte n'est pas actif");
            }
        }

        // 2) Client authentifié
        var clientOpt = clientAuthentifieRepository.findByEmail(email);
        if (clientOpt.isPresent()) {
            ClientAuthentifie client = clientOpt.get();

            if (client.getStatut() == null || client.getStatut() == StatutUtilisateur.ACTIF) {
                if (client.getMot_de_passe() != null
                        && passwordEncoder.matches(password, client.getMot_de_passe())) {

                    client.setDate_derniere_connection(new Date());
                    clientAuthentifieRepository.save(client);

                    String token = jwtUtil.generateClientToken(
                            client.getId_utilisateur(), client.getEmail()
                    );

                    log.info("Connexion universelle Client réussie : clientId={}, email={}",
                            client.getId_utilisateur(), email);

                    return new AuthResponseDTO(
                            token, "Bearer", client.getId_utilisateur(), client.getEmail(),
                            "CLIENT", InterfaceType.CLIENT, null, null,
                            List.of(), false, null
                    );
                }
            } else {
                throw new AccountInactiveException("Ce compte client n'est pas actif");
            }
        }

        // 3) SuperAdmin
        var superAdminOpt = superAdminRepository.findByEmail(email);
        if (superAdminOpt.isPresent()) {
            SuperAdmin superAdmin = superAdminOpt.get();

            if (superAdmin.getStatut() == null || superAdmin.getStatut() == StatutUtilisateur.ACTIF) {
                if (superAdmin.getMot_de_passe() != null
                        && passwordEncoder.matches(password, superAdmin.getMot_de_passe())) {

                    String token = jwtUtil.generateSuperAdminToken(
                            superAdmin.getId_utilisateur(), superAdmin.getEmail()
                    );

                    log.info("Connexion universelle SUPERADMIN réussie : id={}, email={}",
                            superAdmin.getId_utilisateur(), email);

                    return new AuthResponseDTO(
                            token, "Bearer", superAdmin.getId_utilisateur(), superAdmin.getEmail(),
                            "SUPERADMIN", InterfaceType.SUPERADMIN, null, null,
                            List.of(), false, null
                    );
                }
            } else {
                throw new AccountInactiveException("Ce compte super-admin n'est pas actif");
            }
        }

        throw new InvalidCredentialsException("Email ou mot de passe incorrect");
    }

    // =========================================================
    // LOGIN BACK OFFICE
    // =========================================================

    @Override
    public AuthResponseDTO loginBackOffice(
            BackOfficeLoginDTO request) {

        // -----------------------------------------------------
        // Validation
        // -----------------------------------------------------

        if (request == null) {
            throw new IllegalArgumentException(
                    "Les informations de connexion sont obligatoires"
            );
        }

        if (request.getEmail() == null ||
                request.getEmail().isBlank()) {

            throw new IllegalArgumentException(
                    "L'email est obligatoire"
            );
        }

        if (request.getMot_de_passe() == null ||
                request.getMot_de_passe().isBlank()) {

            throw new IllegalArgumentException(
                    "Le mot de passe est obligatoire"
            );
        }


        // -----------------------------------------------------
        // Normalisation email
        // -----------------------------------------------------

        String email = request.getEmail()
                .trim()
                .toLowerCase();


        // -----------------------------------------------------
        // Recherche employé
        // -----------------------------------------------------

        Employee employee =
                employeeRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new InvalidCredentialsException(
                                        "Email ou mot de passe incorrect"
                                )
                        );


        // -----------------------------------------------------
        // Vérification statut
        // -----------------------------------------------------

        if (employee.getStatut() != null &&
                employee.getStatut() != StatutUtilisateur.ACTIF) {

            throw new AccountInactiveException(
                    "Ce compte n'est pas actif"
            );
        }


        // -----------------------------------------------------
        // Vérification mot de passe
        // -----------------------------------------------------

        if (employee.getMot_de_passe() == null ||
                !passwordEncoder.matches(
                        request.getMot_de_passe(),
                        employee.getMot_de_passe()
                )) {

            throw new RuntimeException(
                    "Email ou mot de passe incorrect"
            );
        }


        // -----------------------------------------------------
        // Rôle
        // -----------------------------------------------------

        Role role = employee.getRole();

        if (role == null) {

            throw new AccessDeniedException(
                    "Aucun rôle n'est associé à cet employé"
            );
        }


        // -----------------------------------------------------
        // Vérification accès Back Office
        // -----------------------------------------------------

        if (!Boolean.TRUE.equals(
                role.getAcces_backoffice()
        )) {

            throw new AccessDeniedException(
                    "Vous n'avez pas accès au Back Office"
            );
        }


        // -----------------------------------------------------
        // Permissions Back Office
        // -----------------------------------------------------

        List<String> permissions =
                getPermissions(
                        role,
                        InterfaceType.BACKOFFICE
                );


        // -----------------------------------------------------
        // Récupération PDV + restaurant
        //
        // pointDeVenteId : uniquement si l'employé est affecté à
        // un PDV (ex : Serveur). Pour un ADMIN, reste null.
        //
        // restaurantId : TOUJOURS pris sur employee.getRestaurant()
        // (champ non-nullable en base — chaque Employee appartient
        // obligatoirement à un restaurant, qu'il soit affecté à un
        // PDV ou non). Avant ce correctif, restaurantId n'était
        // dérivé QUE via pdvAffecte -> restaurant, ce qui le
        // laissait à null pour un ADMIN sans PDV et empêchait ces
        // comptes de se connecter côté front (qui refuse un login
        // sans restaurantId).
        // -----------------------------------------------------

        Long pointDeVenteId = null;
        Long restaurantId = null;

        PointDeVente employeePdv =
                employee.getPdvAffecte();

        if (employeePdv != null) {

            pointDeVenteId =
                    employeePdv.getId_pdv();
        }

        if (employee.getRestaurant() != null) {

            restaurantId =
                    employee
                            .getRestaurant()
                            .getId_restaurant();
        }


        // -----------------------------------------------------
        // Date dernière connexion
        // -----------------------------------------------------

        employee.setDate_derniere_connection(
                new Date()
        );

        employeeRepository.save(employee);


        // -----------------------------------------------------
        // JWT
        // -----------------------------------------------------

        String token =
                jwtUtil.generateToken(
                        employee.getId_utilisateur(),
                        employee.getEmail(),
                        role.getNom_role(),
                        InterfaceType.BACKOFFICE,
                        permissions
                );


        // -----------------------------------------------------
        // Log
        // -----------------------------------------------------

        log.info(
                "Connexion Back Office réussie : employeeId={}, email={}, role={}, pdvId={}, restaurantId={}",
                employee.getId_utilisateur(),
                employee.getEmail(),
                role.getNom_role(),
                pointDeVenteId,
                restaurantId
        );


        // -----------------------------------------------------
        // Réponse
        // -----------------------------------------------------

        return new AuthResponseDTO(
                token,
                "Bearer",
                employee.getId_utilisateur(),
                employee.getEmail(),
                role.getNom_role(),
                InterfaceType.BACKOFFICE,
                pointDeVenteId,
                restaurantId,
                permissions,
                false,
                null
        );
    }


    // =========================================================
    // LOGIN PDV
    // =========================================================

    @Override
    public AuthResponseDTO loginPdv(
            PdvLoginDTO request)
            throws PointDeVenteNotFoundException {

        // -----------------------------------------------------
        // Validation
        // -----------------------------------------------------

        if (request == null) {

            throw new IllegalArgumentException(
                    "Les informations de connexion sont obligatoires"
            );
        }

        if (request.getCodePin() == null ||
                request.getCodePin().isBlank()) {

            throw new IllegalArgumentException(
                    "Le code PIN est obligatoire"
            );
        }

        if (request.getPointDeVenteId() == null) {

            throw new IllegalArgumentException(
                    "Le point de vente est obligatoire"
            );
        }


        // -----------------------------------------------------
        // Récupération du PDV
        // -----------------------------------------------------

        PointDeVente pdv =
                dtoMapper.fromPointDeVenteDTO(
                        pointDeVenteService.getPointDeVente(
                                request.getPointDeVenteId()
                        )
                );


        if (pdv == null) {

            throw new PointDeVenteNotFoundException(
                    "Point de vente introuvable"
            );
        }


        // -----------------------------------------------------
        // Employés affectés au PDV
        // -----------------------------------------------------

        List<Employee> employees =
                employeeRepository
                        .findByPdvAffecte(pdv);


        if (employees == null ||
                employees.isEmpty()) {

            throw new RuntimeException(
                    "Aucun employé n'est affecté à ce point de vente"
            );
        }


        // -----------------------------------------------------
        // Recherche par PIN
        // -----------------------------------------------------

        Employee employee =
                employees.stream()

                        .filter(e ->
                                e.getCodePin() != null &&
                                        !e.getCodePin().isBlank()
                        )

                        .filter(e ->
                                passwordEncoder.matches(
                                        request.getCodePin(),
                                        e.getCodePin()
                                )
                        )

                        .findFirst()

                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Code PIN incorrect"
                                )
                        );


        // -----------------------------------------------------
        // Statut
        // -----------------------------------------------------

        if (employee.getStatut() != null &&
                employee.getStatut() != StatutUtilisateur.ACTIF) {

            throw new AccountInactiveException(
                    "Ce compte n'est pas actif"
            );
        }


        // -----------------------------------------------------
        // Rôle
        // -----------------------------------------------------

        Role role = employee.getRole();

        if (role == null) {

            throw new AccessDeniedException(
                    "Aucun rôle n'est associé à cet employé"
            );
        }


        // -----------------------------------------------------
        // Accès PDV
        // -----------------------------------------------------

        if (!Boolean.TRUE.equals(
                role.getAcces_pdv()
        )) {

            throw new RuntimeException(
                    "Vous n'avez pas accès au PDV"
            );
        }


        // -----------------------------------------------------
        // Vérification PDV affecté
        // -----------------------------------------------------

        PointDeVente employeePdv =
                employee.getPdvAffecte();

        if (employeePdv == null) {

            throw new RuntimeException(
                    "Aucun point de vente n'est affecté à cet employé"
            );
        }


        // -----------------------------------------------------
        // Vérification correspondance PDV
        // -----------------------------------------------------

        if (!employeePdv.getId_pdv()
                .equals(pdv.getId_pdv())) {

            throw new RuntimeException(
                    "Cet employé n'est pas affecté à ce point de vente"
            );
        }


        // -----------------------------------------------------
        // Restaurant
        // -----------------------------------------------------

        Long restaurantId = null;

        if (pdv.getRestaurant() != null) {

            restaurantId =
                    pdv.getRestaurant()
                            .getId_restaurant();
        }


        // -----------------------------------------------------
        // Permissions PDV
        // -----------------------------------------------------

        List<String> permissions =
                getPermissions(
                        role,
                        InterfaceType.PDV
                );


        // -----------------------------------------------------
        // Date dernière connexion
        // -----------------------------------------------------

        employee.setDate_derniere_connection(
                new Date()
        );

        employeeRepository.save(employee);


        // -----------------------------------------------------
        // JWT
        // -----------------------------------------------------

        String token =
                jwtUtil.generateToken(
                        employee.getId_utilisateur(),
                        employee.getEmail(),
                        role.getNom_role(),
                        InterfaceType.PDV,
                        permissions
                );


        // -----------------------------------------------------
        // Log
        // -----------------------------------------------------

        log.info(
                "Connexion PDV réussie : employeeId={}, email={}, role={}, pdvId={}, restaurantId={}",
                employee.getId_utilisateur(),
                employee.getEmail(),
                role.getNom_role(),
                pdv.getId_pdv(),
                restaurantId
        );


        // -----------------------------------------------------
        // Réponse
        // -----------------------------------------------------

        return new AuthResponseDTO(
                token,
                "Bearer",
                employee.getId_utilisateur(),
                employee.getEmail(),
                role.getNom_role(),
                InterfaceType.PDV,
                pdv.getId_pdv(),
                restaurantId,
                permissions,
                false,
                null
        );
    }


    // =========================================================
    // LOGIN CLIENT
    // =========================================================

    @Override
    public AuthResponseDTO loginClient(
            ClientLoginDTO request) {

        // -----------------------------------------------------
        // Validation
        // -----------------------------------------------------

        if (request == null) {

            throw new IllegalArgumentException(
                    "Les informations de connexion sont obligatoires"
            );
        }

        if (request.getEmail() == null ||
                request.getEmail().isBlank()) {

            throw new IllegalArgumentException(
                    "L'email est obligatoire"
            );
        }

        if (request.getMot_de_passe() == null ||
                request.getMot_de_passe().isBlank()) {

            throw new IllegalArgumentException(
                    "Le mot de passe est obligatoire"
            );
        }


        // -----------------------------------------------------
        // Normalisation email
        // -----------------------------------------------------

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();


        // -----------------------------------------------------
        // Recherche client
        // -----------------------------------------------------

        ClientAuthentifie client =
                clientAuthentifieRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new InvalidCredentialsException(
                                        "Email ou mot de passe incorrect"
                                )
                        );


        // -----------------------------------------------------
        // Statut
        // -----------------------------------------------------

        if (client.getStatut() != null &&
                client.getStatut() != StatutUtilisateur.ACTIF) {

            throw new RuntimeException(
                    "Ce compte client n'est pas actif"
            );
        }


        // -----------------------------------------------------
        // Mot de passe
        // -----------------------------------------------------

        if (client.getMot_de_passe() == null ||
                !passwordEncoder.matches(
                        request.getMot_de_passe(),
                        client.getMot_de_passe()
                )) {

            throw new RuntimeException(
                    "Email ou mot de passe incorrect"
            );
        }


        // -----------------------------------------------------
        // Date dernière connexion
        // -----------------------------------------------------

        client.setDate_derniere_connection(
                new Date()
        );

        clientAuthentifieRepository.save(client);


        // -----------------------------------------------------
        // JWT Client
        // -----------------------------------------------------

        String token =
                jwtUtil.generateClientToken(
                        client.getId_utilisateur(),
                        client.getEmail()
                );


        // -----------------------------------------------------
        // Log
        // -----------------------------------------------------

        log.info(
                "Connexion client réussie : clientId={}, email={}",
                client.getId_utilisateur(),
                client.getEmail()
        );


        // -----------------------------------------------------
        // Réponse client
        // -----------------------------------------------------

        return new AuthResponseDTO(
                token,
                "Bearer",
                client.getId_utilisateur(),
                client.getEmail(),
                "CLIENT",
                InterfaceType.CLIENT,
                null,
                // BUG CORRIGÉ : restaurantId était toujours renvoyé à null,
                // alors que ClientAuthentifie.restaurant est obligatoire
                // (nullable = false) — le frontend n'avait donc aucun moyen
                // de savoir à quel restaurant le client connecté appartient.
                client.getRestaurant() != null
                        ? client.getRestaurant().getId_restaurant()
                        : null,
                List.of(),
                false,
                null
        );
    }


    // =========================================================
    // INSCRIPTION CLIENT (PUBLIQUE)
    // =========================================================

    @Override
    public AuthResponseDTO registerClient(
            ClientRegisterDTO request)
            throws RestaurantNotFoundException {

        // -----------------------------------------------------
        // Validation
        // -----------------------------------------------------

        if (request == null) {
            throw new IllegalArgumentException(
                    "Les informations d'inscription sont obligatoires"
            );
        }

        if (request.getEmail() == null ||
                request.getEmail().isBlank()) {
            throw new IllegalArgumentException(
                    "L'email est obligatoire"
            );
        }

        if (request.getMot_de_passe() == null ||
                request.getMot_de_passe().isBlank()) {
            throw new IllegalArgumentException(
                    "Le mot de passe est obligatoire"
            );
        }

        if (request.getNom() == null ||
                request.getNom().isBlank()) {
            throw new IllegalArgumentException(
                    "Le nom est obligatoire"
            );
        }

        if (request.getRestaurantId() == null) {
            throw new IllegalArgumentException(
                    "Le restaurant est obligatoire"
            );
        }


        // -----------------------------------------------------
        // Normalisation email
        // -----------------------------------------------------

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();


        // -----------------------------------------------------
        // Email déjà utilisé ?
        // -----------------------------------------------------

        if (clientAuthentifieRepository
                .findByEmail(email)
                .isPresent()) {

            throw new EmailEnUtilisationException(
                    "Un compte existe déjà avec cet email"
            );
        }


        // -----------------------------------------------------
        // Restaurant
        // -----------------------------------------------------

        Restaurant restaurant =
                restaurantRepository
                        .findById(request.getRestaurantId())
                        .orElseThrow(() ->
                                new RestaurantNotFoundException(
                                        "Restaurant introuvable avec l'id : "
                                                + request.getRestaurantId()
                                )
                        );


        // -----------------------------------------------------
        // Création du compte
        // -----------------------------------------------------

        ClientAuthentifie client = new ClientAuthentifie();

        client.setNom(request.getNom().trim());

        if (request.getPrenom() != null) {
            client.setPrenom(request.getPrenom().trim());
        }

        client.setEmail(email);

        client.setMot_de_passe(
                passwordEncoder.encode(
                        request.getMot_de_passe()
                )
        );

        client.setTelephone(request.getTelephone());

        client.setStatut(StatutUtilisateur.ACTIF);

        client.setDate_creation(new Date());

        client.setDate_derniere_connection(new Date());

        client.setRestaurant(restaurant);
ClientAuthentifie savedClient =
                clientAuthentifieRepository.save(client);


        // -----------------------------------------------------
        // Log
        // -----------------------------------------------------

        log.info(
                "Inscription client réussie : clientId={}, email={}, restaurantId={}",
                savedClient.getId_utilisateur(),
                savedClient.getEmail(),
                restaurant.getId_restaurant()
        );


        // -----------------------------------------------------
        // JWT Client (connexion automatique après inscription)
        // -----------------------------------------------------

        String token =
                jwtUtil.generateClientToken(
                        savedClient.getId_utilisateur(),
                        savedClient.getEmail()
                );


        // -----------------------------------------------------
        // Réponse
        // -----------------------------------------------------

        return new AuthResponseDTO(
                token,
                "Bearer",
                savedClient.getId_utilisateur(),
                savedClient.getEmail(),
                "CLIENT",
                InterfaceType.CLIENT,
                null,
                restaurant.getId_restaurant(),
                List.of(),
                false,
                null
        );
    }


    // =========================================================
    // LOGIN SUPER-ADMIN
    // =========================================================

    @Override
    public AuthResponseDTO loginSuperAdmin(
            SuperAdminLoginDTO request) {

        // -----------------------------------------------------
        // Validation
        // -----------------------------------------------------

        if (request == null) {

            throw new IllegalArgumentException(
                    "Les informations de connexion sont obligatoires"
            );
        }

        if (request.getEmail() == null ||
                request.getEmail().isBlank()) {

            throw new IllegalArgumentException(
                    "L'email est obligatoire"
            );
        }

        if (request.getMot_de_passe() == null ||
                request.getMot_de_passe().isBlank()) {

            throw new IllegalArgumentException(
                    "Le mot de passe est obligatoire"
            );
        }


        // -----------------------------------------------------
        // Normalisation email
        // -----------------------------------------------------

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();


        // -----------------------------------------------------
        // Recherche super-admin
        // -----------------------------------------------------

        SuperAdmin superAdmin =
                superAdminRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new InvalidCredentialsException(
                                        "Email ou mot de passe incorrect"
                                )
                        );


        // -----------------------------------------------------
        // Statut
        // -----------------------------------------------------

        if (superAdmin.getStatut() != null &&
                superAdmin.getStatut() != StatutUtilisateur.ACTIF) {

            throw new AccountInactiveException(
                    "Ce compte n'est pas actif"
            );
        }


        // -----------------------------------------------------
        // Mot de passe
        // -----------------------------------------------------

        if (superAdmin.getMot_de_passe() == null ||
                !passwordEncoder.matches(
                        request.getMot_de_passe(),
                        superAdmin.getMot_de_passe()
                )) {

            throw new RuntimeException(
                    "Email ou mot de passe incorrect"
            );
        }


        // -----------------------------------------------------
        // Date dernière connexion
        // -----------------------------------------------------

        superAdmin.setDate_derniere_connection(
                new Date()
        );

        superAdminRepository.save(superAdmin);


        // -----------------------------------------------------
        // JWT super-admin
        //
        // Pas de restaurant, pas de PDV, pas de permissions
        // (Role/RoleFonctionnalite) : le rôle SUPERADMIN donne
        // l'accès complet à RestaurantController, rien d'autre.
        // -----------------------------------------------------

        String token =
                jwtUtil.generateSuperAdminToken(
                        superAdmin.getId_utilisateur(),
                        superAdmin.getEmail()
                );


        // -----------------------------------------------------
        // Log
        // -----------------------------------------------------

        log.info(
                "Connexion super-admin réussie : superAdminId={}, email={}",
                superAdmin.getId_utilisateur(),
                superAdmin.getEmail()
        );


        // -----------------------------------------------------
        // Réponse
        // -----------------------------------------------------

        return new AuthResponseDTO(
                token,
                "Bearer",
                superAdmin.getId_utilisateur(),
                superAdmin.getEmail(),
                "SUPERADMIN",
                InterfaceType.SUPERADMIN,
                null,
                null,
                List.of(),
                false,
                null
        );
    }


    // =========================================================
    // SELECTION RESTAURANT SUPERADMIN
    // =========================================================

    @Override
    public AuthResponseDTO selectRestaurant(Long restaurantId)
            throws RestaurantNotFoundException {

        if (restaurantId == null) {
            throw new IllegalArgumentException("L'identifiant du restaurant est obligatoire");
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_SUPERADMIN".equalsIgnoreCase(a.getAuthority()))) {
            throw new AccessDeniedException("Seul le SUPERADMIN peut sélectionner un restaurant");
        }

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(
                        "Aucun restaurant avec l'id : " + restaurantId));

        String email = authentication.getName();

        SuperAdmin superAdmin = superAdminRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Super Admin introuvable : " + email));

        // Le SUPERADMIN garde son rôle.
        // L'interface devient BACKOFFICE pour accéder aux APIs du restaurant.
        // Toutes les fonctionnalités Back Office sont autorisées.
        List<String> permissions = fonctionnaliteRepository.findByDisponibleBackoffice(true)
                .stream()
                .map(f -> f.getCodeFonctionnalite())
                .filter(code -> code != null && !code.isBlank())
                .map(String::toUpperCase)
                .distinct()
                .toList();

        String token = jwtUtil.generateSuperAdminToken(
                superAdmin.getId_utilisateur(),
                superAdmin.getEmail(),
                restaurantId,
                InterfaceType.BACKOFFICE,
                permissions
        );

        log.info(
                "Restaurant sélectionné par SUPERADMIN : superAdminId={}, restaurantId={}, restaurantNom={}",
                superAdmin.getId_utilisateur(),
                restaurantId,
                restaurant.getNomRestaurant()
        );

        return new AuthResponseDTO(
                token,
                "Bearer",
                superAdmin.getId_utilisateur(),
                superAdmin.getEmail(),
                "SUPERADMIN",
                InterfaceType.BACKOFFICE,
                null,
                restaurantId,
                permissions,
                false,
                null
        );
    }


    // =========================================================
    // PERMISSIONS
    // =========================================================

    private List<String> getPermissions(
            Role role,
            InterfaceType interfaceType) {

        if (role == null ||
                interfaceType == null) {

            return List.of();
        }


        return roleFonctionnaliteRepository
                .findByRoleAndInterfaceTypeAndAutoriseTrue(
                        role,
                        interfaceType
                )

                .stream()

                .map(
                        RoleFonctionnalite::getFonctionnalite
                )

                .filter(
                        fonctionnalite ->
                                fonctionnalite != null
                )

                .map(
                        fonctionnalite ->
                                fonctionnalite
                                        .getCodeFonctionnalite()
                )

                .filter(
                        code ->
                                code != null &&
                                        !code.isBlank()
                )

                .map(String::toUpperCase)

                .distinct()

                .toList();
    }
}
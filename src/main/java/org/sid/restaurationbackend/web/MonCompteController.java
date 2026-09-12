package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import org.sid.restaurationbackend.dtos.MonCompteDTO;
import org.sid.restaurationbackend.entities.ClientAuthentifie;
import org.sid.restaurationbackend.entities.Employee;
import org.sid.restaurationbackend.entities.Utilisateur;
import org.sid.restaurationbackend.exceptions.EmployeeNotFoundException;
import org.sid.restaurationbackend.exceptions.UtilisateurNotFoundException;
import org.sid.restaurationbackend.repositories.UtilisateurRepository;
import org.sid.restaurationbackend.services.CurrentUserService;
import org.sid.restaurationbackend.services.FileStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * =============================================================
 * "MON COMPTE" — AUTO-MODIFICATION DU COMPTE CONNECTÉ
 * =============================================================
 *
 * Contrairement à EmployeeController (réservé aux Employee, et dont
 * les endpoints d'écriture sont réservés au SUPERADMIN pour gérer
 * N'IMPORTE QUEL employé), ce contrôleur sert uniquement la page
 * "Mon compte" : chaque utilisateur authentifié (Employee,
 * SuperAdmin, ClientAuthentifie) lit/modifie SES PROPRES
 * informations personnelles. Tous les champs concernés (nom,
 * prénom, email, téléphone, photo, mot de passe) sont portés par la
 * classe de base Utilisateur — rien de spécifique à Employee n'est
 * modifiable ici, donc ça fonctionne nativement pour tous les types
 * de compte, y compris SuperAdmin qui n'est PAS un Employee (voir
 * historique : un premier essai basé sur EmployeeController
 * renvoyait un 404 pour tout compte SUPERADMIN).
 *
 * L'utilisateur modifié n'est jamais déduit de l'URL ou du corps de
 * la requête : uniquement du token JWT, via
 * CurrentUserService.getUtilisateurConnecte(). Aucune règle
 * spécifique n'est nécessaire dans SecurityConfig : la règle
 * générale .anyRequest().authenticated() suffit, tout utilisateur
 * authentifié pouvant lire/modifier SON PROPRE compte.
 */
@RestController
@RequestMapping("/api/mon-compte")
@AllArgsConstructor
public class MonCompteController {

    private final CurrentUserService currentUserService;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;


    // Récupérer le compte connecté
    @GetMapping
    public ResponseEntity<MonCompteDTO> getMonCompte()
            throws UtilisateurNotFoundException, EmployeeNotFoundException {

        Utilisateur utilisateur = currentUserService.getUtilisateurConnecte();

        return ResponseEntity.ok(toDto(utilisateur));
    }


    // Modifier le compte connecté
    @PutMapping
    public ResponseEntity<MonCompteDTO> updateMonCompte(
            @RequestBody MonCompteDTO dto)
            throws UtilisateurNotFoundException, EmployeeNotFoundException {

        Utilisateur utilisateur = currentUserService.getUtilisateurConnecte();

        String motDePasseActuel = utilisateur.getMot_de_passe();

        utilisateur.setNom(dto.getNom());
        utilisateur.setPrenom(dto.getPrenom());
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setTelephone(dto.getTelephone());
        utilisateur.setPhoto_profil(dto.getPhoto_profil());

        // Champs propres à ClientAuthentifie : adresse / allergie /
        // preferences sont modifiables par le client lui-même.

        // JAMAIS repris de dto ici (voir javadoc MonCompteDTO) : un
        // client ne doit pas pouvoir se créditer lui-même des points
        // ou changer son propre code de parrainage via ce endpoint
        // générique — ces deux champs restent gérés exclusivement par
        // ClientAuthentifieController côté back-office.
        if (utilisateur instanceof ClientAuthentifie client) {
            client.setAdresse(dto.getAdresse());
            client.setAllergie(dto.getAllergie());
            client.setPreferences(dto.getPreferences());
            client.setDate_modification_profil(new java.util.Date());
        }

        // Le front renvoie soit le hash existant tel quel (champ non
        // modifié par l'utilisateur), soit un NOUVEAU mot de passe en
        // clair saisi par l'utilisateur (même convention que
        // EmployeeServiceImpl.updateEmployee). On ne hache que s'il a
        // réellement changé, pour ne pas le hacher deux fois — et on
        // ne l'écrase jamais par une valeur vide.
        String motDePasseEnvoye = dto.getMot_de_passe();

        if (motDePasseEnvoye != null && !motDePasseEnvoye.isBlank()
                && !motDePasseEnvoye.equals(motDePasseActuel)) {

            utilisateur.setMot_de_passe(passwordEncoder.encode(motDePasseEnvoye));
        } else {
            utilisateur.setMot_de_passe(motDePasseActuel);
        }

        Utilisateur sauvegarde = utilisateurRepository.save(utilisateur);

        return ResponseEntity.ok(toDto(sauvegarde));
    }


    // Uploader (remplacer) la photo de profil du compte connecté.
    //
    // Contrairement à updateMonCompte (PUT), la photo est ici
    // enregistrée immédiatement sur le disque et persistée en base
    // dès l'upload — le frontend n'a pas besoin de cliquer sur
    // "Enregistrer" pour que la nouvelle photo soit prise en compte,
    // ce qui évite d'avoir à combiner multipart/form-data et JSON
    // dans une seule requête.
    @PostMapping(
            value = "/photo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadPhoto(
            @RequestParam("file") MultipartFile file)
            throws UtilisateurNotFoundException, EmployeeNotFoundException {

        Utilisateur utilisateur = currentUserService.getUtilisateurConnecte();

        String url;

        try {
            url = fileStorageService.enregistrerImage(
                    file,
                    "photos-profil",
                    "user-" + utilisateur.getId_utilisateur()
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(java.util.Map.of("message", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of(
                            "message",
                            "Impossible d'enregistrer l'image."
                    ));
        }

        utilisateur.setPhoto_profil(url);

        Utilisateur sauvegarde = utilisateurRepository.save(utilisateur);

        return ResponseEntity.ok(toDto(sauvegarde));
    }


    // =========================================================
    // MAPPING
    // =========================================================

    private MonCompteDTO toDto(Utilisateur utilisateur) {

        MonCompteDTO dto = new MonCompteDTO();

        dto.setId_utilisateur(utilisateur.getId_utilisateur());
        dto.setNom(utilisateur.getNom());
        dto.setPrenom(utilisateur.getPrenom());
        dto.setEmail(utilisateur.getEmail());
        dto.setTelephone(utilisateur.getTelephone());
        dto.setMot_de_passe(utilisateur.getMot_de_passe());
        dto.setPhoto_profil(utilisateur.getPhoto_profil());

        if (utilisateur instanceof Employee employee) {
            dto.setMatricule(employee.getMatricule());

            if (employee.getRole() != null) {
                dto.setNomRole(employee.getRole().getNom_role());
            }
        }

        if (utilisateur instanceof ClientAuthentifie client) {
            dto.setAdresse(client.getAdresse());
            dto.setAllergie(client.getAllergie());
            dto.setPreferences(client.getPreferences());
            dto.setCodeParrainage(client.getCodeParrainage());
        }

        return dto;
    }
}
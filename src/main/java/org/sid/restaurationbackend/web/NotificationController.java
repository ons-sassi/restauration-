package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import org.sid.restaurationbackend.dtos.NotificationDTO;
import org.sid.restaurationbackend.dtos.UtilisateurDTO;
import org.sid.restaurationbackend.entities.Employee;
import org.sid.restaurationbackend.entities.Utilisateur;
import org.sid.restaurationbackend.enums.StatutEnvoi;
import org.sid.restaurationbackend.enums.TypeNotification;
import org.sid.restaurationbackend.exceptions.AccesNotificationNonAutoriseException;
import org.sid.restaurationbackend.exceptions.EmployeeNotFoundException;
import org.sid.restaurationbackend.exceptions.NotificationNotFoundException;
import org.sid.restaurationbackend.exceptions.UtilisateurNotFoundException;
import org.sid.restaurationbackend.repositories.UtilisateurRepository;
import org.sid.restaurationbackend.services.CurrentUserService;
import org.sid.restaurationbackend.services.NotificationService;
import org.sid.restaurationbackend.services.UtilisateurService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * =========================================================
 * SÉCURITÉ MULTI-RESTAURANT (voir CurrentUserService) — Lot 2.6
 * =========================================================
 * Notification n'a PAS de colonne restaurant. emetteur et destinataire
 * sont tous les deux de type Utilisateur (héritage JOINED) : ça peut
 * être un Employee (rattaché à un restaurant), un ClientAuthentifie
 * (jamais rattaché à un restaurant) ou un SuperAdmin.
 *
 * AVANT ce correctif, ce contrôleur était intégralement ouvert : aucune
 * vérification nulle part. C'était un IDOR pur — tous les endpoints
 * filtrant par destinataire/émetteur acceptaient un id arbitraire
 * fourni par l'appelant, et getAllNotifications()/getNotificationsByStatut/
 * getNotificationsByType renvoyaient TOUT, tous restaurants et tous
 * utilisateurs confondus.
 *
 * RÈGLE DE SCOPING RETENUE (hybride, cf. javadoc de
 * verifierAccesUtilisateurCible ci-dessous) :
 *   - SUPERADMIN : bypass total (voit tout, y compris orphelins).
 *   - Un utilisateur (employé, client...) peut toujours consulter les
 *     notifications dont IL EST émetteur ou destinataire (ownership).
 *   - Un Employee peut en plus consulter celles d'un collègue Employee
 *     DU MÊME RESTAURANT (même logique de filtrage en mémoire que les
 *     autres contrôleurs de ce chantier — utile pour une vue back-office
 *     "notifications de mon restaurant").
 *   - Sinon (ex : un client authentifié consultant les notifications
 *     d'un autre utilisateur) : refusé (403).
 *
 * NOTE FRONTEND : le NotificationService Angular actuel appelle déjà
 * /destinataire, /emetteur et /destinataire/recentes avec un simple
 * ID en query param (HttpParams), et NON un UtilisateurDTO complet en
 * @RequestBody comme l'ancien contrôleur l'exigeait (GET+body est de
 * toute façon peu fiable selon les clients HTTP). Les signatures ont
 * donc été corrigées en @RequestParam Long, ce qui aligne le contrat
 * réel avec l'appel Angular — sans changement de comportement observable
 * côté app, puisqu'aucun composant Angular n'utilise encore ce service
 * (vérifié : zéro consommateur de NotificationService dans le code
 * actuel, cette fonctionnalité n'est pas encore branchée à l'UI).
 *
 * NON COUVERT PAR CE LOT : saveNotification() ne vérifie pas que
 * l'émetteur fourni est bien l'utilisateur connecté (même famille de
 * limite que ReclamationController.saveReclamation — aucune notion
 * d'ownership à la création dans ce projet). On recharge toutefois les
 * VRAIS émetteur/destinataire par leur id pour ne jamais persister un
 * sous-objet UtilisateurDTO falsifié (nom, statut, etc. fournis par le
 * frontend).
 */
@RestController
@RequestMapping("/api/notifications")
@AllArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUserService currentUserService;
    private final UtilisateurService utilisateurService;

    // Nécessaire pour recharger le VRAI sous-type (Employee / Client /
    // SuperAdmin) d'un id cible arbitraire : UtilisateurService ne
    // renvoie qu'un UtilisateurDTO générique (RestaurantMapper
    // .fromUtilisateur recopie toujours dans un "new Utilisateur()",
    // ce qui perd le sous-type). Aucun autre contrôleur n'injecte de
    // repository directement, mais aucun service existant n'expose
    // l'entité polymorphe — exception documentée à ce pattern.
    private final UtilisateurRepository utilisateurRepository;


    // ---------------------------------------------------------------
    // Utilitaires internes de scoping (voir javadoc classe)
    // ---------------------------------------------------------------

    private boolean estSuperAdminConnecte() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_SUPERADMIN".equals(
                                authority.getAuthority()
                        )
                );
    }

    /**
     * Vérifie que l'utilisateur CONNECTE a le droit de consulter/agir
     * sur des notifications concernant l'utilisateur CIBLE (id
     * quelconque, émetteur ou destinataire d'une notification).
     *
     * Autorisé si :
     *  - l'appelant est SUPERADMIN (bypass total) ;
     *  - idCible correspond à l'utilisateur connecté lui-même
     *    (ownership — vaut pour un Employee comme pour un client) ;
     *  - l'appelant est un Employee ET la cible est un Employee du
     *    MEME restaurant (consultation "collègue", même restaurant).
     *
     * Refusé dans tous les autres cas, y compris si idCible est null
     * (on ne devine jamais une autorisation).
     */
    private boolean estAutoriseSurUtilisateurCible(
            Long idCible,
            boolean superAdminConnecte,
            Utilisateur connecte) {

        if (idCible == null) {
            return false;
        }

        if (superAdminConnecte) {
            return true;
        }

        if (connecte == null) {
            return false;
        }

        if (idCible.equals(connecte.getId_utilisateur())) {
            return true;
        }

        if (connecte instanceof Employee employeeConnecte
                && employeeConnecte.getRestaurant() != null) {

            Utilisateur cible =
                    utilisateurRepository.findById(idCible).orElse(null);

            return cible instanceof Employee employeeCible
                    && employeeCible.getRestaurant() != null
                    && employeeCible.getRestaurant().getId_restaurant()
                            .equals(employeeConnecte.getRestaurant()
                                    .getId_restaurant());
        }

        return false;
    }

    private void verifierAccesUtilisateurCible(
            Long idCible,
            boolean superAdminConnecte,
            Utilisateur connecte)
            throws AccesNotificationNonAutoriseException {

        if (!estAutoriseSurUtilisateurCible(
                idCible, superAdminConnecte, connecte)) {

            throw new AccesNotificationNonAutoriseException(
                    "Accès refusé : ces notifications ne concernent ni "
                            + "l'utilisateur connecté, ni un collègue de "
                            + "son restaurant.");
        }
    }

    // À utiliser sur une NotificationDTO déjà rechargée depuis le
    // service (jamais sur un objet envoyé tel quel par le frontend).
    private void verifierAccesNotification(
            NotificationDTO notification,
            boolean superAdminConnecte,
            Utilisateur connecte)
            throws AccesNotificationNonAutoriseException {

        Long destId = notification != null
                && notification.getDestinataire() != null
                ? notification.getDestinataire().getId_utilisateur()
                : null;

        Long emetId = notification != null
                && notification.getEmetteur() != null
                ? notification.getEmetteur().getId_utilisateur()
                : null;

        if (estAutoriseSurUtilisateurCible(destId, superAdminConnecte, connecte)
                || estAutoriseSurUtilisateurCible(emetId, superAdminConnecte, connecte)) {
            return;
        }

        throw new AccesNotificationNonAutoriseException(
                "Accès refusé : cette notification ne concerne ni "
                        + "l'utilisateur connecté, ni un collègue de son "
                        + "restaurant.");
    }

    // Connecté courant, chargé une seule fois par requête (jamais dans
    // une boucle de filtrage) — null si l'appelant est SUPERADMIN
    // (pas besoin de le recharger dans ce cas, cf. bypass).
    private Utilisateur chargerConnecteSiBesoin(boolean superAdminConnecte)
            throws UtilisateurNotFoundException, EmployeeNotFoundException {

        return superAdminConnecte
                ? null
                : currentUserService.getUtilisateurConnecte();
    }

    // Recharge le VRAI utilisateur (par id) sous forme de DTO générique,
    // pour ne jamais faire confiance à un sous-objet destinataire/
    // émetteur fourni tel quel par le frontend (nom, statut... falsifiables).
    private UtilisateurDTO chargerUtilisateurDTOAutorise(
            Long id,
            boolean superAdminConnecte,
            Utilisateur connecte)
            throws UtilisateurNotFoundException,
            AccesNotificationNonAutoriseException {

        verifierAccesUtilisateurCible(id, superAdminConnecte, connecte);

        return utilisateurService.getUtilisateurById(id);
    }


    // =========================================================
    // CREATE
    // =========================================================

    /**
     * Créer une notification. Pas de vérification d'ownership sur
     * l'émetteur (voir javadoc de la classe — hors périmètre, comme
     * pour ReclamationController.saveReclamation) : on recharge
     * toutefois les vrais émetteur/destinataire par leur id pour ne
     * jamais persister un sous-objet UtilisateurDTO falsifié.
     */
    @PostMapping
    public ResponseEntity<NotificationDTO> saveNotification(
            @RequestBody NotificationDTO notificationDTO)
            throws UtilisateurNotFoundException {

        if (notificationDTO.getDestinataire() != null
                && notificationDTO.getDestinataire().getId_utilisateur() != null) {

            notificationDTO.setDestinataire(
                    utilisateurService.getUtilisateurById(
                            notificationDTO.getDestinataire().getId_utilisateur()));
        }

        if (notificationDTO.getEmetteur() != null
                && notificationDTO.getEmetteur().getId_utilisateur() != null) {

            notificationDTO.setEmetteur(
                    utilisateurService.getUtilisateurById(
                            notificationDTO.getEmetteur().getId_utilisateur()));
        }

        return new ResponseEntity<>(
                notificationService.saveNotification(notificationDTO),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationDTO> getNotification(
            @PathVariable Long id)
            throws NotificationNotFoundException, UtilisateurNotFoundException,
            AccesNotificationNonAutoriseException, EmployeeNotFoundException {

        NotificationDTO notification =
                notificationService.getNotification(id);

        boolean superAdmin = estSuperAdminConnecte();
        Utilisateur connecte = chargerConnecteSiBesoin(superAdmin);

        verifierAccesNotification(notification, superAdmin, connecte);

        return ResponseEntity.ok(notification);
    }

    // Avant : renvoyait TOUTES les notifications, tous restaurants et
    // tous utilisateurs confondus. Filtrée maintenant par ownership /
    // restaurant (voir javadoc classe), sauf pour le SUPERADMIN.
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getAllNotifications()
            throws UtilisateurNotFoundException, EmployeeNotFoundException {

        List<NotificationDTO> toutes =
                notificationService.getAllNotifications();

        boolean superAdmin = estSuperAdminConnecte();

        if (superAdmin) {
            return ResponseEntity.ok(toutes);
        }

        Utilisateur connecte = chargerConnecteSiBesoin(false);

        return ResponseEntity.ok(
                toutes.stream()
                        .filter(n -> estAutoriseSurUtilisateurCible(
                                n.getDestinataire() != null
                                        ? n.getDestinataire().getId_utilisateur()
                                        : null,
                                false, connecte)
                                || estAutoriseSurUtilisateurCible(
                                n.getEmetteur() != null
                                        ? n.getEmetteur().getId_utilisateur()
                                        : null,
                                false, connecte))
                        .toList()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotificationDTO> updateNotification(
            @PathVariable Long id,
            @RequestBody NotificationDTO notificationDTO)
            throws NotificationNotFoundException, UtilisateurNotFoundException,
            AccesNotificationNonAutoriseException, EmployeeNotFoundException {

        NotificationDTO existante = notificationService.getNotification(id);

        boolean superAdmin = estSuperAdminConnecte();
        Utilisateur connecte = chargerConnecteSiBesoin(superAdmin);

        verifierAccesNotification(existante, superAdmin, connecte);

        // Si le corps de la requête rattache la notification à un
        // AUTRE destinataire/émetteur, la cible doit elle aussi être
        // autorisée (jamais suivie aveuglément) et rechargée par son id.
        Long ancienDestId = existante.getDestinataire() != null
                ? existante.getDestinataire().getId_utilisateur() : null;
        Long ancienEmetId = existante.getEmetteur() != null
                ? existante.getEmetteur().getId_utilisateur() : null;

        if (notificationDTO.getDestinataire() != null
                && notificationDTO.getDestinataire().getId_utilisateur() != null
                && !Objects.equals(
                        notificationDTO.getDestinataire().getId_utilisateur(),
                        ancienDestId)) {

            notificationDTO.setDestinataire(
                    chargerUtilisateurDTOAutorise(
                            notificationDTO.getDestinataire().getId_utilisateur(),
                            superAdmin, connecte));
        }

        if (notificationDTO.getEmetteur() != null
                && notificationDTO.getEmetteur().getId_utilisateur() != null
                && !Objects.equals(
                        notificationDTO.getEmetteur().getId_utilisateur(),
                        ancienEmetId)) {

            notificationDTO.setEmetteur(
                    chargerUtilisateurDTOAutorise(
                            notificationDTO.getEmetteur().getId_utilisateur(),
                            superAdmin, connecte));
        }

        return ResponseEntity.ok(
                notificationService.updateNotification(id, notificationDTO)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id)
            throws NotificationNotFoundException, UtilisateurNotFoundException,
            AccesNotificationNonAutoriseException, EmployeeNotFoundException {

        NotificationDTO existante = notificationService.getNotification(id);

        boolean superAdmin = estSuperAdminConnecte();
        Utilisateur connecte = chargerConnecteSiBesoin(superAdmin);

        verifierAccesNotification(existante, superAdmin, connecte);

        notificationService.deleteNotification(id);

        return ResponseEntity.noContent().build();
    }

    // IDOR corrigé : avant, un UtilisateurDTO arbitraire (n'importe quel
    // id_utilisateur) était accepté tel quel. Signature alignée sur
    // l'appel Angular réel (@RequestParam, voir javadoc classe) : l'id
    // fourni est maintenant vérifié (ownership / collègue même
    // restaurant) puis rechargé depuis la base avant d'être transmis
    // au service.
    @GetMapping("/destinataire")
    public ResponseEntity<List<NotificationDTO>>
    getNotificationsByDestinataire(
            @RequestParam Long destinataireId)
            throws UtilisateurNotFoundException,
            AccesNotificationNonAutoriseException, EmployeeNotFoundException {

        boolean superAdmin = estSuperAdminConnecte();
        Utilisateur connecte = chargerConnecteSiBesoin(superAdmin);

        UtilisateurDTO destinataire = chargerUtilisateurDTOAutorise(
                destinataireId, superAdmin, connecte);

        return ResponseEntity.ok(
                notificationService
                        .getNotificationsByDestinataire(destinataire)
        );
    }

    @GetMapping("/emetteur")
    public ResponseEntity<List<NotificationDTO>>
    getNotificationsByEmetteur(
            @RequestParam Long emetteurId)
            throws UtilisateurNotFoundException,
            AccesNotificationNonAutoriseException, EmployeeNotFoundException {

        boolean superAdmin = estSuperAdminConnecte();
        Utilisateur connecte = chargerConnecteSiBesoin(superAdmin);

        UtilisateurDTO emetteur = chargerUtilisateurDTOAutorise(
                emetteurId, superAdmin, connecte);

        return ResponseEntity.ok(
                notificationService
                        .getNotificationsByEmetteur(emetteur)
        );
    }

    // Avant : renvoyait TOUTES les notifications ayant ce statut, tous
    // restaurants et tous utilisateurs confondus (aucun paramètre de
    // scoping dans cet endpoint). Filtrée maintenant comme
    // getAllNotifications().
    @GetMapping("/statut/{statutEnvoi}")
    public ResponseEntity<List<NotificationDTO>>
    getNotificationsByStatut(
            @PathVariable StatutEnvoi statutEnvoi)
            throws UtilisateurNotFoundException, EmployeeNotFoundException {

        List<NotificationDTO> resultats =
                notificationService.getNotificationsByStatut(statutEnvoi);

        boolean superAdmin = estSuperAdminConnecte();

        if (superAdmin) {
            return ResponseEntity.ok(resultats);
        }

        Utilisateur connecte = chargerConnecteSiBesoin(false);

        return ResponseEntity.ok(
                resultats.stream()
                        .filter(n -> estAutoriseSurUtilisateurCible(
                                n.getDestinataire() != null
                                        ? n.getDestinataire().getId_utilisateur()
                                        : null,
                                false, connecte)
                                || estAutoriseSurUtilisateurCible(
                                n.getEmetteur() != null
                                        ? n.getEmetteur().getId_utilisateur()
                                        : null,
                                false, connecte))
                        .toList()
        );
    }

    // Même remarque que getNotificationsByStatut ci-dessus.
    @GetMapping("/type/{type}")
    public ResponseEntity<List<NotificationDTO>>
    getNotificationsByType(
            @PathVariable TypeNotification type)
            throws UtilisateurNotFoundException, EmployeeNotFoundException {

        List<NotificationDTO> resultats =
                notificationService.getNotificationsByType(type);

        boolean superAdmin = estSuperAdminConnecte();

        if (superAdmin) {
            return ResponseEntity.ok(resultats);
        }

        Utilisateur connecte = chargerConnecteSiBesoin(false);

        return ResponseEntity.ok(
                resultats.stream()
                        .filter(n -> estAutoriseSurUtilisateurCible(
                                n.getDestinataire() != null
                                        ? n.getDestinataire().getId_utilisateur()
                                        : null,
                                false, connecte)
                                || estAutoriseSurUtilisateurCible(
                                n.getEmetteur() != null
                                        ? n.getEmetteur().getId_utilisateur()
                                        : null,
                                false, connecte))
                        .toList()
        );
    }

    @GetMapping("/destinataire/statut/{statutEnvoi}")
    public ResponseEntity<List<NotificationDTO>>
    getNotificationsByDestinataireAndStatut(
            @RequestParam Long destinataireId,
            @PathVariable StatutEnvoi statutEnvoi)
            throws UtilisateurNotFoundException,
            AccesNotificationNonAutoriseException, EmployeeNotFoundException {

        boolean superAdmin = estSuperAdminConnecte();
        Utilisateur connecte = chargerConnecteSiBesoin(superAdmin);

        UtilisateurDTO destinataire = chargerUtilisateurDTOAutorise(
                destinataireId, superAdmin, connecte);

        return ResponseEntity.ok(
                notificationService
                        .getNotificationsByDestinataireAndStatut(
                                destinataire,
                                statutEnvoi
                        )
        );
    }

    @GetMapping("/destinataire/type/{type}")
    public ResponseEntity<List<NotificationDTO>>
    getNotificationsByDestinataireAndType(
            @RequestParam Long destinataireId,
            @PathVariable TypeNotification type)
            throws UtilisateurNotFoundException,
            AccesNotificationNonAutoriseException, EmployeeNotFoundException {

        boolean superAdmin = estSuperAdminConnecte();
        Utilisateur connecte = chargerConnecteSiBesoin(superAdmin);

        UtilisateurDTO destinataire = chargerUtilisateurDTOAutorise(
                destinataireId, superAdmin, connecte);

        return ResponseEntity.ok(
                notificationService
                        .getNotificationsByDestinataireAndType(
                                destinataire,
                                type
                        )
        );
    }

    @GetMapping("/destinataire/recentes")
    public ResponseEntity<List<NotificationDTO>>
    getNotificationsDestinataireRecentes(
            @RequestParam Long destinataireId)
            throws UtilisateurNotFoundException,
            AccesNotificationNonAutoriseException, EmployeeNotFoundException {

        boolean superAdmin = estSuperAdminConnecte();
        Utilisateur connecte = chargerConnecteSiBesoin(superAdmin);

        UtilisateurDTO destinataire = chargerUtilisateurDTOAutorise(
                destinataireId, superAdmin, connecte);

        return ResponseEntity.ok(
                notificationService
                        .getNotificationsDestinataireRecentes(
                                destinataire
                        )
        );
    }
}

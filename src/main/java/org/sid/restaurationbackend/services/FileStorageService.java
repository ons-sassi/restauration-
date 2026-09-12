package org.sid.restaurationbackend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/**
 * =============================================================
 * STOCKAGE LOCAL DES FICHIERS UPLOADÉS
 * =============================================================
 *
 * Sert pour le moment uniquement à la photo de profil (voir
 * MonCompteController.uploadPhoto), mais reste volontairement
 * générique (sous-dossier passé en paramètre) pour être réutilisable
 * plus tard (logo restaurant, image produit, etc.).
 *
 * Les fichiers sont écrits sur le disque, dans un dossier racine
 * "uploads" à la racine du projet (voir application.properties,
 * propriété app.upload.dir). Ils sont ensuite exposés publiquement
 * en lecture via WebConfig (resource handler "/uploads/**") et
 * SecurityConfig (permitAll en GET sur "/uploads/**"), car une balise
 * &lt;img&gt; ne peut pas envoyer le header Authorization.
 *
 * L'URL renvoyée est donc une URL RELATIVE (ex.
 * "/uploads/photos-profil/xxx.jpg") : c'est au frontend de la
 * préfixer avec l'origine du backend (pas avec la base "/api") pour
 * construire l'URL absolue affichée dans un &lt;img [src]&gt;.
 */
@Service
public class FileStorageService {

    private static final List<String> EXTENSIONS_AUTORISEES =
            List.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    private static final long TAILLE_MAX_OCTETS = 5L * 1024 * 1024; // 5 Mo

    private final Path racineUpload;

    public FileStorageService(
            @Value("${app.upload.dir:uploads}") String dossierUpload) {

        this.racineUpload = Paths.get(dossierUpload)
                .toAbsolutePath()
                .normalize();
    }

    /**
     * Enregistre une image dans le sous-dossier donné et renvoie
     * l'URL relative publique permettant de la récupérer ensuite
     * (via le resource handler "/uploads/**").
     *
     * @param fichier       fichier reçu (multipart/form-data)
     * @param sousDossier   ex. "photos-profil"
     * @param prefixeNom    utilisé pour préfixer le nom généré
     *                      (ex. "user-12"), simple aide au débogage
     * @throws IllegalArgumentException si le fichier est vide,
     *         n'est pas une image, ou dépasse la taille maximale
     */
    public String enregistrerImage(
            MultipartFile fichier,
            String sousDossier,
            String prefixeNom) throws IOException {

        if (fichier == null || fichier.isEmpty()) {
            throw new IllegalArgumentException(
                    "Aucun fichier fourni."
            );
        }

        if (fichier.getSize() > TAILLE_MAX_OCTETS) {
            throw new IllegalArgumentException(
                    "L'image ne doit pas dépasser 5 Mo."
            );
        }

        String contentType = fichier.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException(
                    "Le fichier doit être une image (jpg, png, gif ou webp)."
            );
        }

        String nomOriginal = fichier.getOriginalFilename();
        String extension = "";

        if (nomOriginal != null && nomOriginal.contains(".")) {
            extension = nomOriginal
                    .substring(nomOriginal.lastIndexOf('.'))
                    .toLowerCase();
        }

        if (!EXTENSIONS_AUTORISEES.contains(extension)) {
            throw new IllegalArgumentException(
                    "Format d'image non supporté. Formats acceptés : jpg, jpeg, png, gif, webp."
            );
        }

        Path dossierDestination = racineUpload.resolve(sousDossier);
        Files.createDirectories(dossierDestination);

        String nomFichier = prefixeNom
                + "-" + UUID.randomUUID()
                + extension;

        Path destination = dossierDestination.resolve(nomFichier);

        Files.copy(
                fichier.getInputStream(),
                destination,
                StandardCopyOption.REPLACE_EXISTING
        );

        // URL relative : le frontend la préfixe avec l'origine du
        // backend (pas "/api") pour obtenir une URL absolue.
        return "/uploads/" + sousDossier + "/" + nomFichier;
    }
}

package org.sid.restaurationbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Expose le dossier "uploads" (voir FileStorageService) en lecture
 * publique sous "/uploads/**", pour que les &lt;img [src]&gt; du
 * frontend puissent charger les photos de profil sans avoir besoin
 * d'envoyer le token JWT (impossible depuis une balise img).
 *
 * Voir aussi SecurityConfig, qui doit autoriser GET "/uploads/**"
 * en permitAll pour que ce contenu statique soit réellement
 * accessible sans authentification.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads}")
    private String dossierUpload;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String cheminAbsolu = Paths.get(dossierUpload)
                .toAbsolutePath()
                .normalize()
                .toString();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + cheminAbsolu + "/");
    }
}

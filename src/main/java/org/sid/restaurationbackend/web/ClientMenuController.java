package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;

import org.sid.restaurationbackend.dtos.CategorieDTO;
import org.sid.restaurationbackend.dtos.ModificateurDTO;
import org.sid.restaurationbackend.dtos.ProduitDTO;

import org.sid.restaurationbackend.exceptions.CategorieNotFoundException;
import org.sid.restaurationbackend.exceptions.ClientNotFoundException;
import org.sid.restaurationbackend.exceptions.ProduitNotFoundException;

import org.sid.restaurationbackend.services.ClientMenuService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Menu (catégories / produits) pour l'espace client.
 *
 * Sous "/api/client/**" (voir SecurityConfig : hasAuthority
 * "INTERFACE_CLIENT"), donc réservé aux ClientAuthentifie connectés —
 * même modèle de sécurité que ClientCommandeController et
 * MonCompteController.
 *
 * ⚠️ Ne PAS réutiliser ProduitController ("/api/produits") pour
 * l'espace client : ses endpoints GET dérivent le restaurant via
 * CurrentUserService.getRestaurantIdConnecte(), qui suppose un
 * Employee (ou SUPERADMIN) connecté et lève EmployeeNotFoundException
 * pour un ClientAuthentifie. Ici, le restaurant est systématiquement
 * dérivé du ClientAuthentifie connecté (voir ClientMenuServiceImpl).
 */
@RestController
@RequestMapping("/api/client/menu")
@AllArgsConstructor
public class ClientMenuController {

    private final ClientMenuService clientMenuService;

    @GetMapping("/categories")
    public ResponseEntity<List<CategorieDTO>> getCategoriesRacines()
            throws ClientNotFoundException {

        return ResponseEntity.ok(
                clientMenuService.getCategoriesRacines()
        );
    }

    @GetMapping("/categories/{id}/sous-categories")
    public ResponseEntity<List<CategorieDTO>> getSousCategories(
            @PathVariable Long id)
            throws ClientNotFoundException, CategorieNotFoundException {

        return ResponseEntity.ok(
                clientMenuService.getSousCategories(id)
        );
    }

    @GetMapping("/produits")
    public ResponseEntity<List<ProduitDTO>> getProduits(
            @RequestParam(required = false) Long categorieId)
            throws ClientNotFoundException, CategorieNotFoundException {

        return ResponseEntity.ok(
                clientMenuService.getProduits(categorieId)
        );
    }

    @GetMapping("/produits/{id}")
    public ResponseEntity<ProduitDTO> getProduit(
            @PathVariable Long id)
            throws ClientNotFoundException, ProduitNotFoundException {

        return ResponseEntity.ok(
                clientMenuService.getProduit(id)
        );
    }

    @GetMapping("/produits/{id}/modificateurs")
    public ResponseEntity<List<ModificateurDTO>> getModificateurs(
            @PathVariable Long id)
            throws ClientNotFoundException, ProduitNotFoundException {

        return ResponseEntity.ok(
                clientMenuService.getModificateurs(id)
        );
    }

    @GetMapping("/meilleures-ventes")
    public ResponseEntity<List<ProduitDTO>> getMeilleuresVentes(
            @RequestParam(defaultValue = "8") int limite)
            throws ClientNotFoundException {

        return ResponseEntity.ok(
                clientMenuService.getMeilleuresVentes(limite)
        );
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<ProduitDTO>> getSuggestions(
            @RequestParam(defaultValue = "8") int limite)
            throws ClientNotFoundException {

        return ResponseEntity.ok(
                clientMenuService.getSuggestions(limite)
        );
    }
}

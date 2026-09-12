package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import org.sid.restaurationbackend.dtos.FonctionnaliteDTO;
import org.sid.restaurationbackend.dtos.ModuleDTO;
import org.sid.restaurationbackend.exceptions.FonctionnaliteNotFoundException;
import org.sid.restaurationbackend.exceptions.ModuleNotFoundException;
import org.sid.restaurationbackend.services.ModuleFonctionnaliteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modules-fonctionnalites")
@AllArgsConstructor
public class ModuleFonctionnaliteController {

    private final ModuleFonctionnaliteService service;

    @PostMapping("/modules")
    public ResponseEntity<ModuleDTO> saveModule(
            @RequestBody ModuleDTO moduleDTO) {

        return new ResponseEntity<>(
                service.saveModule(moduleDTO),
                HttpStatus.CREATED
        );
    }


    @PutMapping("/modules/{id}")
    public ResponseEntity<ModuleDTO> updateModule(
            @PathVariable Long id,
            @RequestBody ModuleDTO moduleDTO)
            throws ModuleNotFoundException {

        return ResponseEntity.ok(
                service.updateModule(id, moduleDTO)
        );
    }


    @GetMapping("/modules/{id}")
    public ResponseEntity<ModuleDTO> getModuleById(
            @PathVariable Long id)
            throws ModuleNotFoundException {

        return ResponseEntity.ok(
                service.getModuleById(id)
        );
    }


    @GetMapping("/modules")
    public ResponseEntity<List<ModuleDTO>> getAllModules() {

        return ResponseEntity.ok(
                service.getAllModules()
        );
    }


    @GetMapping("/modules/search")
    public ResponseEntity<List<ModuleDTO>> searchModules(
            @RequestParam(required = false) String keyword) {

        return ResponseEntity.ok(
                service.searchModules(keyword)
        );
    }


    @GetMapping("/modules/nom/{nom}")
    public ResponseEntity<ModuleDTO> getModuleByNom(
            @PathVariable String nom)
            throws ModuleNotFoundException {

        return ResponseEntity.ok(
                service.getModuleByNom(nom)
        );
    }


    @DeleteMapping("/modules/{id}")
    public ResponseEntity<Void> deleteModule(
            @PathVariable Long id)
            throws ModuleNotFoundException {

        service.deleteModule(id);

        return ResponseEntity.noContent().build();
    }




    @GetMapping("/modules/{id}/disponible-pdv")
    public ResponseEntity<Boolean> disponiblePdv(
            @PathVariable Long id)
            throws ModuleNotFoundException {

        return ResponseEntity.ok(
                service.disponiblePdv(id)
        );
    }


    @GetMapping("/modules/{id}/disponible-backoffice")
    public ResponseEntity<Boolean> disponibleBackOffice(
            @PathVariable Long id)
            throws ModuleNotFoundException {

        return ResponseEntity.ok(
                service.disponibleBackOffice(id)
        );
    }


    @PatchMapping("/modules/{id}/activer-pdv")
    public ResponseEntity<ModuleDTO> activerPourPdv(
            @PathVariable Long id)
            throws ModuleNotFoundException {

        return ResponseEntity.ok(
                service.activerPourPdv(id)
        );
    }


    @PatchMapping("/modules/{id}/desactiver-pdv")
    public ResponseEntity<ModuleDTO> desactiverPourPdv(
            @PathVariable Long id)
            throws ModuleNotFoundException {

        return ResponseEntity.ok(
                service.desactiverPourPdv(id)
        );
    }


    @PatchMapping("/modules/{id}/activer-backoffice")
    public ResponseEntity<ModuleDTO> activerPourBackOffice(
            @PathVariable Long id)
            throws ModuleNotFoundException {

        return ResponseEntity.ok(
                service.activerPourBackOffice(id)
        );
    }


    @PatchMapping("/modules/{id}/desactiver-backoffice")
    public ResponseEntity<ModuleDTO> desactiverPourBackOffice(
            @PathVariable Long id)
            throws ModuleNotFoundException {

        return ResponseEntity.ok(
                service.desactiverPourBackOffice(id)
        );
    }



    @PostMapping("/fonctionnalites")
    public ResponseEntity<FonctionnaliteDTO> saveFonctionnalite(
            @RequestBody FonctionnaliteDTO fonctionnaliteDTO) {

        return new ResponseEntity<>(
                service.saveFonctionnalite(fonctionnaliteDTO),
                HttpStatus.CREATED
        );
    }


    @PutMapping("/fonctionnalites/{id}")
    public ResponseEntity<FonctionnaliteDTO> updateFonctionnalite(
            @PathVariable Long id,
            @RequestBody FonctionnaliteDTO fonctionnaliteDTO)
            throws FonctionnaliteNotFoundException {

        return ResponseEntity.ok(
                service.updateFonctionnalite(
                        id,
                        fonctionnaliteDTO
                )
        );
    }


    @GetMapping("/fonctionnalites/{id}")
    public ResponseEntity<FonctionnaliteDTO>
    getFonctionnaliteById(
            @PathVariable Long id)
            throws FonctionnaliteNotFoundException {

        return ResponseEntity.ok(
                service.getFonctionnaliteById(id)
        );
    }


    @GetMapping("/fonctionnalites")
    public ResponseEntity<List<FonctionnaliteDTO>>
    getAllFonctionnalites() {

        return ResponseEntity.ok(
                service.getAllFonctionnalites()
        );
    }


    @GetMapping("/fonctionnalites/search")
    public ResponseEntity<List<FonctionnaliteDTO>>
    searchFonctionnalites(
            @RequestParam(required = false) String keyword) {

        return ResponseEntity.ok(
                service.searchFonctionnalites(keyword)
        );
    }


    @DeleteMapping("/fonctionnalites/{id}")
    public ResponseEntity<Void> deleteFonctionnalite(
            @PathVariable Long id)
            throws FonctionnaliteNotFoundException {

        service.deleteFonctionnalite(id);

        return ResponseEntity.noContent().build();
    }



    @PostMapping("/modules/{moduleId}/fonctionnalites/{fonctionnaliteId}")
    public ResponseEntity<Void> assignModuleToFonctionnalite(
            @PathVariable Long moduleId,
            @PathVariable Long fonctionnaliteId)
            throws FonctionnaliteNotFoundException,
            ModuleNotFoundException {

        service.assignModuleToFonctionnalite(
                moduleId,
                fonctionnaliteId
        );

        return ResponseEntity.ok().build();
    }


    @GetMapping("/modules/{moduleId}/fonctionnalites")
    public ResponseEntity<List<FonctionnaliteDTO>>
    getFonctionnalitesByModule(
            @PathVariable Long moduleId)
            throws ModuleNotFoundException {

        ModuleDTO module =
                service.getModuleById(moduleId);

        return ResponseEntity.ok(
                service.getFonctionnalitesByModule(module)
        );
    }




    @PostMapping(
            "/fonctionnalites/{parentId}/sous-fonctionnalites/{fonctionnaliteId}"
    )
    public ResponseEntity<Void>
    assignFonctionnaliteToFonctionnaliteParent(
            @PathVariable Long parentId,
            @PathVariable Long fonctionnaliteId)
            throws FonctionnaliteNotFoundException {

        service.assignFonctionnaliteToFonctionnaliteParent(
                parentId,
                fonctionnaliteId
        );

        return ResponseEntity.ok().build();
    }


    @GetMapping(
            "/fonctionnalites/{parentId}/sous-fonctionnalites"
    )
    public ResponseEntity<List<FonctionnaliteDTO>>
    getSousFonctionnalites(
            @PathVariable Long parentId)
            throws FonctionnaliteNotFoundException {

        FonctionnaliteDTO parent =
                service.getFonctionnaliteById(parentId);

        return ResponseEntity.ok(
                service.getFonctionnalitesByFonctionnaliteParent(
                        parent
                )
        );
    }




    @GetMapping(
            "/fonctionnalites/{id}/disponible-pdv"
    )
    public ResponseEntity<Boolean>
    fonctionnaliteDisponiblePdv(
            @PathVariable Long id)
            throws FonctionnaliteNotFoundException {

        return ResponseEntity.ok(
                service.fonctionnaliteDisponiblePdv(id)
        );
    }


    @GetMapping(
            "/fonctionnalites/{id}/disponible-backoffice"
    )
    public ResponseEntity<Boolean>
    fonctionnaliteDisponibleBackOffice(
            @PathVariable Long id)
            throws FonctionnaliteNotFoundException {

        return ResponseEntity.ok(
                service.fonctionnaliteDisponibleBackOffice(id)
        );
    }


    @PatchMapping(
            "/fonctionnalites/{id}/activer-pdv"
    )
    public ResponseEntity<FonctionnaliteDTO>
    activerFonctionnalitePdv(
            @PathVariable Long id)
            throws FonctionnaliteNotFoundException {

        return ResponseEntity.ok(
                service.activerFonctionnalitePdv(id)
        );
    }


    @PatchMapping(
            "/fonctionnalites/{id}/desactiver-pdv"
    )
    public ResponseEntity<FonctionnaliteDTO>
    desactiverFonctionnalitePdv(
            @PathVariable Long id)
            throws FonctionnaliteNotFoundException {

        return ResponseEntity.ok(
                service.desactiverFonctionnalitePdv(id)
        );
    }


    @PatchMapping(
            "/fonctionnalites/{id}/activer-backoffice"
    )
    public ResponseEntity<FonctionnaliteDTO>
    activerFonctionnaliteBackOffice(
            @PathVariable Long id)
            throws FonctionnaliteNotFoundException {

        return ResponseEntity.ok(
                service.activerFonctionnaliteBackOffice(id)
        );
    }


    @PatchMapping(
            "/fonctionnalites/{id}/desactiver-backoffice"
    )
    public ResponseEntity<FonctionnaliteDTO>
    desactiverFonctionnaliteBackOffice(
            @PathVariable Long id)
            throws FonctionnaliteNotFoundException {

        return ResponseEntity.ok(
                service.desactiverFonctionnaliteBackOffice(id)
        );
    }
}
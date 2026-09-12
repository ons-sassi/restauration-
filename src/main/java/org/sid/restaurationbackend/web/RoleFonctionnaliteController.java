package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import org.sid.restaurationbackend.dtos.*;
import org.sid.restaurationbackend.enums.InterfaceType;
import org.sid.restaurationbackend.exceptions.RoleFonctionnaliteNotFoundException;
import org.sid.restaurationbackend.services.RoleFonctionnaliteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roleFonctionnalites")
@AllArgsConstructor
public class RoleFonctionnaliteController {

    private final RoleFonctionnaliteService roleFonctionnaliteService;




    @PostMapping
    public ResponseEntity<RoleFonctionnaliteDTO> saveRoleFonctionnalite(
            @RequestBody RoleFonctionnaliteDTO dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(roleFonctionnaliteService
                        .saveRoleFonctionnalite(dto));
    }




    @PutMapping("/{id}")
    public ResponseEntity<RoleFonctionnaliteDTO> updateRoleFonctionnalite(
            @PathVariable Long id,
            @RequestBody RoleFonctionnaliteDTO dto)
            throws RoleFonctionnaliteNotFoundException {

        return ResponseEntity.ok(
                roleFonctionnaliteService
                        .updateRoleFonctionnalite(id, dto)
        );
    }




    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoleFonctionnalite(
            @PathVariable Long id)
            throws RoleFonctionnaliteNotFoundException {

        roleFonctionnaliteService
                .deleteRoleFonctionnalite(id);

        return ResponseEntity.noContent().build();
    }




    @GetMapping("/{id}")
    public ResponseEntity<RoleFonctionnaliteDTO>
    getRoleFonctionnalite(
            @PathVariable Long id)
            throws RoleFonctionnaliteNotFoundException {

        return ResponseEntity.ok(
                roleFonctionnaliteService
                        .getRoleFonctionnalite(id)
        );
    }



    @GetMapping
    public ResponseEntity<List<RoleFonctionnaliteDTO>>
    getAllRoleFonctionnalites() {

        return ResponseEntity.ok(
                roleFonctionnaliteService
                        .getAllRoleFonctionnalites()
        );
    }




    @PostMapping("/by-role")
    public ResponseEntity<List<RoleFonctionnaliteDTO>>
    getByRole(@RequestBody RoleDTO roleDTO) {

        return ResponseEntity.ok(
                roleFonctionnaliteService
                        .getByRole(roleDTO)
        );
    }



    @PostMapping("/by-fonctionnalite")
    public ResponseEntity<List<RoleFonctionnaliteDTO>>
    getByFonctionnalite(
            @RequestBody FonctionnaliteDTO fonctionnaliteDTO) {

        return ResponseEntity.ok(
                roleFonctionnaliteService
                        .getByFonctionnalite(
                                fonctionnaliteDTO)
        );
    }




    @GetMapping("/by-interface/{interfaceType}")
    public ResponseEntity<List<RoleFonctionnaliteDTO>>
    getByInterfaceType(
            @PathVariable InterfaceType interfaceType) {

        return ResponseEntity.ok(
                roleFonctionnaliteService
                        .getByInterfaceType(interfaceType)
        );
    }



    @PostMapping("/by-role/interface")
    public ResponseEntity<List<RoleFonctionnaliteDTO>>
    getByRoleAndInterface(
            @RequestBody RoleDTO roleDTO,
            @RequestParam InterfaceType interfaceType) {

        return ResponseEntity.ok(
                roleFonctionnaliteService
                        .getByRoleAndInterface(
                                roleDTO,
                                interfaceType)
        );
    }




    @PostMapping("/by-fonctionnalite/interface")
    public ResponseEntity<List<RoleFonctionnaliteDTO>>
    getByFonctionnaliteAndInterface(
            @RequestBody FonctionnaliteDTO fonctionnaliteDTO,
            @RequestParam InterfaceType interfaceType) {

        return ResponseEntity.ok(
                roleFonctionnaliteService
                        .getByFonctionnaliteAndInterface(
                                fonctionnaliteDTO,
                                interfaceType)
        );
    }




    @PostMapping("/autorisees")
    public ResponseEntity<List<RoleFonctionnaliteDTO>>
    getAutoriseesByRoleAndInterface(
            @RequestBody RoleDTO roleDTO,
            @RequestParam InterfaceType interfaceType) {

        return ResponseEntity.ok(
                roleFonctionnaliteService
                        .getAutoriseesByRoleAndInterface(
                                roleDTO,
                                interfaceType)
        );
    }



    @PostMapping("/assigner")
    public ResponseEntity<RoleFonctionnaliteDTO>
    assignerFonctionnalite(
            @RequestParam Long roleId,
            @RequestParam Long fonctionnaliteId,
            @RequestParam InterfaceType interfaceType,
            @RequestParam Boolean autorise,
            @RequestParam(required = false) Long employeeId) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                roleFonctionnaliteService
                        .assignerFonctionnalite(
                                roleId,
                                fonctionnaliteId,
                                interfaceType,
                                autorise,
                                employeeId)
        );
    }




    @PatchMapping("/{id}/autorisation")
    public ResponseEntity<Void>
    modifierAutorisation(
            @PathVariable Long id,
            @RequestParam Boolean autorise)
            throws RoleFonctionnaliteNotFoundException {

        roleFonctionnaliteService
                .modifierAutorisation(id, autorise);

        return ResponseEntity.noContent().build();
    }
}
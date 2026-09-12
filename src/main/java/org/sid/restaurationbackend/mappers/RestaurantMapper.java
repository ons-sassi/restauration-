package org.sid.restaurationbackend.mappers;

import org.sid.restaurationbackend.dtos.*;
import org.sid.restaurationbackend.entities.*;
import org.sid.restaurationbackend.entities.Module;
import org.sid.restaurationbackend.enums.ApplicationReduction;
import org.sid.restaurationbackend.enums.TypeReduction;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.FeatureDescriptor;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class RestaurantMapper {

    // =========================================================
    // ==================== Utilisateur ========================
    // =========================================================

    public UtilisateurDTO fromUtilisateur(Utilisateur utilisateur) {

        if (utilisateur == null) {
            return null;
        }

        UtilisateurDTO dto = new UtilisateurDTO();

        BeanUtils.copyProperties(utilisateur, dto);

        return dto;
    }

    public Utilisateur fromUtilisateurDTO(UtilisateurDTO dto) {

        if (dto == null) {
            return null;
        }

        Utilisateur entity = new Utilisateur();

        BeanUtils.copyProperties(dto, entity);

        return entity;
    }

    // =========================================================
    // ==================== Restaurant =========================
    // =========================================================

    public RestaurantDTO fromRestaurant(Restaurant restaurant) {

        if (restaurant == null) {
            return null;
        }

        RestaurantDTO dto = new RestaurantDTO();

        BeanUtils.copyProperties(restaurant, dto);

        return dto;
    }

    public Restaurant fromRestaurantDTO(RestaurantDTO dto) {

        if (dto == null) {
            return null;
        }

        Restaurant entity = new Restaurant();

        BeanUtils.copyProperties(dto, entity);

        return entity;
    }

    // =========================================================
    // ==================== Fournisseur ========================
    // =========================================================

    public FournisseurDTO fromFournisseur(Fournisseur fournisseur) {

        if (fournisseur == null) {
            return null;
        }

        FournisseurDTO dto = new FournisseurDTO();

        BeanUtils.copyProperties(fournisseur, dto);

        // BeanUtils.copyProperties ignore silencieusement "restaurant"
        // (types RestaurantDTO / Restaurant incompatibles) : mapping
        // explicite requis, comme pour ElementMenu/Produit/Categorie.
        dto.setRestaurant(
                fromRestaurant(
                        fournisseur.getRestaurant()
                )
        );

        return dto;
    }

    public Fournisseur fromFournisseurDTO(FournisseurDTO dto) {

        if (dto == null) {
            return null;
        }

        Fournisseur entity = new Fournisseur();

        BeanUtils.copyProperties(dto, entity);

        entity.setRestaurant(
                fromRestaurantDTO(
                        dto.getRestaurant()
                )
        );

        return entity;
    }

    // =========================================================
    // ==================== ModePaiement =======================
    // =========================================================

    public ModePaiementDTO fromModePaiement(ModePaiement modePaiement) {

        if (modePaiement == null) {
            return null;
        }

        ModePaiementDTO dto = new ModePaiementDTO();

        BeanUtils.copyProperties(
                modePaiement,
                dto
        );

        dto.setClient(
                fromClientAuthentifie(
                        modePaiement.getClient()
                )
        );

        return dto;
    }

    public ModePaiement fromModePaiementDTO(ModePaiementDTO dto) {

        if (dto == null) {
            return null;
        }

        ModePaiement entity = new ModePaiement();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setClient(
                fromClientAuthentifieDTO(
                        dto.getClient()
                )
        );

        return entity;
    }

    // =========================================================
    // ==================== Module ==============================
    // =========================================================

    public ModuleDTO fromModule(Module module) {

        if (module == null) {
            return null;
        }

        ModuleDTO dto = new ModuleDTO();

        BeanUtils.copyProperties(
                module,
                dto
        );

        return dto;
    }

    public Module fromModuleDTO(ModuleDTO dto) {

        if (dto == null) {
            return null;
        }

        Module entity = new Module();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        return entity;
    }

    // =========================================================
    // ==================== Reduction ===========================
    // =========================================================

    public ReductionDTO fromReduction(Reduction reduction) {

        if (reduction == null) {
            return null;
        }

        ReductionDTO dto = new ReductionDTO();

        dto.setId_reduction(
                reduction.getId_reduction()
        );

        dto.setRestaurant(
                fromRestaurant(
                        reduction.getRestaurant()
                )
        );

        dto.setNomReduction(
                reduction.getNom_reduction()
        );

        if (reduction.getType() != null) {
            dto.setType(
                    reduction.getType().name()
            );
        }

        dto.setValeur(
                reduction.getValeur()
        );

        dto.setDateDebut(
                reduction.getDateDebut()
        );

        dto.setDateFin(
                reduction.getDateFin()
        );

        dto.setConditionsApplication(
                reduction.getConditions_application()
        );

        dto.setApplicationProduits(
                reduction.getApplicationProduits() != null
                        ? reduction.getApplicationProduits().name()
                        : ApplicationReduction.TOUS_PRODUITS.name()
        );

        if (reduction.getProduits() != null) {
            dto.setProduits(
                    reduction.getProduits().stream()
                            .map(p -> new ReductionDTO.ReductionProduitDTO(
                                    p.getId_element(),
                                    p.getNom()
                            ))
                            .toList()
            );
        }

        dto.setMontantMinimum(
                reduction.getMontantMinimum()
        );

        dto.setAutomatique(
                reduction.getAutomatique()
        );

        dto.setActive(
                reduction.getActive() != null
                        ? reduction.getActive()
                        : true
        );

        dto.setNombreApplicationsAutorise(
                reduction.getNombreApplicationsAutorise()
        );

        dto.setNombreApplicationsEffectuees(
                reduction.getNombreApplicationsEffectuees() != null
                        ? reduction.getNombreApplicationsEffectuees()
                        : 0
        );

        return dto;
    }

    public Reduction fromReductionDTO(ReductionDTO dto) {

        if (dto == null) {
            return null;
        }

        Reduction entity = new Reduction();

        entity.setId_reduction(
                dto.getId_reduction()
        );

        entity.setRestaurant(
                fromRestaurantDTO(
                        dto.getRestaurant()
                )
        );

        entity.setNom_reduction(
                dto.getNomReduction()
        );

        if (dto.getType() != null
                && !dto.getType().isBlank()) {

            try {
                entity.setType(
                        TypeReduction.valueOf(
                                dto.getType().trim().toUpperCase()
                        )
                );
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Type de réduction invalide : "
                                + dto.getType()
                                + ". Valeurs acceptées : POURCENTAGE ou MONTANT_FIXE."
                );
            }
        }

        entity.setValeur(
                dto.getValeur()
        );

        entity.setDateDebut(
                dto.getDateDebut()
        );

        entity.setDateFin(
                dto.getDateFin()
        );

        entity.setConditions_application(
                dto.getConditionsApplication()
        );

        if (dto.getApplicationProduits() != null
                && !dto.getApplicationProduits().isBlank()) {

            try {
                entity.setApplicationProduits(
                        ApplicationReduction.valueOf(
                                dto.getApplicationProduits().trim().toUpperCase()
                        )
                );
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Application de réduction invalide : "
                                + dto.getApplicationProduits()
                                + ". Valeurs acceptées : TOUS_PRODUITS ou PRODUITS_SPECIFIQUES."
                );
            }
        } else {
            entity.setApplicationProduits(ApplicationReduction.TOUS_PRODUITS);
        }

        entity.setMontantMinimum(
                dto.getMontantMinimum()
        );

        entity.setAutomatique(
                dto.getAutomatique() != null ? dto.getAutomatique() : true
        );

        entity.setActive(
                dto.getActive() != null ? dto.getActive() : true
        );

        entity.setNombreApplicationsAutorise(
                dto.getNombreApplicationsAutorise()
        );

        // Une création part toujours d'un compteur à zéro, même si le
        // front envoie une valeur par erreur (champ normalement en
        // lecture seule).
        entity.setNombreApplicationsEffectuees(0);

        // NB : la liste des produits (dto.getProduitsIds()) est résolue et
        // affectée dans ReductionServiceImpl, qui a accès au ProduitRepository.

        return entity;
    }

    // =========================================================
    // ==================== Taxe ================================
    // =========================================================

    public TaxeDTO fromTaxe(Taxe taxe) {

        if (taxe == null) {
            return null;
        }

        // Mapping explicite : l'entité est en camelCase (nomTaxe,
        // applicableA, dateCreation) alors que le DTO est en snake_case
        // (nom_taxe, applicable_a, date_creation). BeanUtils.copyProperties
        // copie par nom de propriété EXACT et ignorait donc ces champs.

        TaxeDTO dto = new TaxeDTO();

        dto.setId_taxe(taxe.getId_taxe());
        dto.setNom_taxe(taxe.getNomTaxe());
        dto.setTaux(taxe.getTaux());
        dto.setApplicable_a(taxe.getApplicableA());
        dto.setDate_creation(taxe.getDateCreation());
        dto.setStatut(taxe.getStatut());

        return dto;
    }

    public Taxe fromTaxeDTO(TaxeDTO dto) {

        if (dto == null) {
            return null;
        }

        Taxe entity = new Taxe();

        entity.setId_taxe(dto.getId_taxe());
        entity.setNomTaxe(dto.getNom_taxe());
        entity.setTaux(dto.getTaux());
        entity.setApplicableA(dto.getApplicable_a());
        entity.setDateCreation(dto.getDate_creation());
        entity.setStatut(dto.getStatut());

        return entity;
    }

    // =========================================================
    // ==================== BonDeCommandeStock =================
    // =========================================================

    public BonDeCommandeStockDTO fromBonDeCommandeStock(
            BonDeCommandeStock entity) {

        if (entity == null) {
            return null;
        }

        BonDeCommandeStockDTO dto =
                new BonDeCommandeStockDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        dto.setFournisseur(
                fromFournisseur(
                        entity.getFournisseur()
                )
        );

        dto.setRestaurant(
                fromRestaurant(
                        entity.getRestaurant()
                )
        );

        return dto;
    }

    public BonDeCommandeStock fromBonDeCommandeStockDTO(
            BonDeCommandeStockDTO dto) {

        if (dto == null) {
            return null;
        }

        BonDeCommandeStock entity =
                new BonDeCommandeStock();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setFournisseur(
                fromFournisseurDTO(
                        dto.getFournisseur()
                )
        );

        entity.setRestaurant(
                fromRestaurantDTO(
                        dto.getRestaurant()
                )
        );

        return entity;
    }

    // =========================================================
    // ==================== ElementMenu =========================
    // =========================================================

    public ElementMenuDTO fromElementMenu(
            ElementMenu entity) {

        if (entity == null) {
            return null;
        }

        ElementMenuDTO dto =
                new ElementMenuDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        dto.setCategorieParent(
                fromCategorie(
                        entity.getCategorieParent()
                )
        );

        dto.setRestaurant(
                fromRestaurant(
                        entity.getRestaurant()
                )
        );

        return dto;
    }

    public ElementMenu fromElementMenuDTO(
            ElementMenuDTO dto) {

        if (dto == null) {
            return null;
        }

        ElementMenu entity =
                new ElementMenu();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setCategorieParent(
                fromCategorieDTO(
                        dto.getCategorieParent()
                )
        );

        entity.setRestaurant(
                fromRestaurantDTO(
                        dto.getRestaurant()
                )
        );

        return entity;
    }

    // =========================================================
    // ==================== Categorie ===========================
    // =========================================================

    public CategorieDTO fromCategorie(
            Categorie entity) {

        if (entity == null) {
            return null;
        }

        CategorieDTO dto =
                new CategorieDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        dto.setCategorieParent(
                fromCategorie(
                        entity.getCategorieParent()
                )
        );

        dto.setRestaurant(
                fromRestaurant(
                        entity.getRestaurant()
                )
        );

        return dto;
    }

    public Categorie fromCategorieDTO(
            CategorieDTO dto) {

        if (dto == null) {
            return null;
        }

        Categorie entity =
                new Categorie();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setCategorieParent(
                fromCategorieDTO(
                        dto.getCategorieParent()
                )
        );

        entity.setRestaurant(
                fromRestaurantDTO(
                        dto.getRestaurant()
                )
        );

        return entity;
    }

    // =========================================================
    // ==================== Produit =============================
    // =========================================================

    public ProduitDTO fromProduit(
            Produit entity) {

        if (entity == null) {
            return null;
        }

        ProduitDTO dto =
                new ProduitDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        dto.setCategorieParent(
                fromCategorie(
                        entity.getCategorieParent()
                )
        );

        dto.setRestaurant(
                fromRestaurant(
                        entity.getRestaurant()
                )
        );

        if (entity.getIngredients() != null) {

            dto.setIngredients(
                    entity.getIngredients()
                            .stream()
                            .map(this::fromIngredient)
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }

    public Produit fromProduitDTO(
            ProduitDTO dto) {

        if (dto == null) {
            return null;
        }

        Produit entity =
                new Produit();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setCategorieParent(
                fromCategorieDTO(
                        dto.getCategorieParent()
                )
        );

        entity.setRestaurant(
                fromRestaurantDTO(
                        dto.getRestaurant()
                )
        );

        if (dto.getIngredients() != null) {

            entity.setIngredients(
                    dto.getIngredients()
                            .stream()
                            .map(this::fromIngredientDTO)
                            .collect(Collectors.toList())
            );
        }

        return entity;
    }

    // =========================================================
    // ==================== Produit Shallow =====================
    // =========================================================

    private ProduitDTO fromProduitShallow(
            Produit entity) {

        if (entity == null) {
            return null;
        }

        ProduitDTO dto =
                new ProduitDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        // On casse la boucle produit -> ingredient -> produits :
        // pas besoin de renvoyer l'image (souvent volumineuse en base64)
        // ni la catégorie/le restaurant complets pour une référence "arrière".
        dto.setImage(null);
        dto.setCategorieParent(null);
        dto.setRestaurant(null);

        return dto;
    }

    private Produit fromProduitDTOShallow(
            ProduitDTO dto) {

        if (dto == null) {
            return null;
        }

        Produit entity =
                new Produit();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setCategorieParent(
                fromCategorieDTO(
                        dto.getCategorieParent()
                )
        );

        entity.setRestaurant(
                fromRestaurantDTO(
                        dto.getRestaurant()
                )
        );

        return entity;
    }

    // =========================================================
    // ==================== Ingredient ==========================
    // =========================================================

    public IngredientDTO fromIngredient(
            Ingredient entity) {

        if (entity == null) {
            return null;
        }

        IngredientDTO dto =
                new IngredientDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        dto.setFournisseur(
                fromFournisseur(
                        entity.getFournisseur()
                )
        );

        dto.setRestaurant(
                fromRestaurant(
                        entity.getRestaurant()
                )
        );

        if (entity.getProduits() != null) {

            dto.setProduits(
                    entity.getProduits()
                            .stream()
                            .map(this::fromProduitShallow)
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }

    public Ingredient fromIngredientDTO(
            IngredientDTO dto) {

        if (dto == null) {
            return null;
        }

        Ingredient entity =
                new Ingredient();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setFournisseur(
                fromFournisseurDTO(
                        dto.getFournisseur()
                )
        );

        entity.setRestaurant(
                fromRestaurantDTO(
                        dto.getRestaurant()
                )
        );

        if (dto.getProduits() != null) {

            entity.setProduits(
                    dto.getProduits()
                            .stream()
                            .map(this::fromProduitDTOShallow)
                            .collect(Collectors.toList())
            );
        }

        return entity;
    }

    // =========================================================
    // ==================== LigneBonDeCommande =================
    // =========================================================

    public LigneBonDeCommandeDTO fromLigneBonDeCommande(
            LigneBonDeCommande entity) {

        if (entity == null) {
            return null;
        }

        LigneBonDeCommandeDTO dto =
                new LigneBonDeCommandeDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        dto.setBonCommande(
                fromBonDeCommandeStock(
                        entity.getBonCommande()
                )
        );

        dto.setIngredient(
                fromIngredient(
                        entity.getIngredient()
                )
        );

        return dto;
    }

    public LigneBonDeCommande fromLigneBonDeCommandeDTO(
            LigneBonDeCommandeDTO dto) {

        if (dto == null) {
            return null;
        }

        LigneBonDeCommande entity =
                new LigneBonDeCommande();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setBonCommande(
                fromBonDeCommandeStockDTO(
                        dto.getBonCommande()
                )
        );

        entity.setIngredient(
                fromIngredientDTO(
                        dto.getIngredient()
                )
        );

        return entity;
    }

    // =========================================================
    // ==================== Fonctionnalite =====================
    // =========================================================

    public FonctionnaliteDTO fromFonctionnalite(
            Fonctionnalite entity) {

        if (entity == null) {
            return null;
        }

        FonctionnaliteDTO dto =
                new FonctionnaliteDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        dto.setModule(
                fromModule(
                        entity.getModule()
                )
        );

        dto.setFonctionnaliteParent(
                fromFonctionnalite(
                        entity.getFonctionnaliteParent()
                )
        );

        return dto;
    }

    public Fonctionnalite fromFonctionnaliteDTO(
            FonctionnaliteDTO dto) {

        if (dto == null) {
            return null;
        }

        Fonctionnalite entity =
                new Fonctionnalite();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setModule(
                fromModuleDTO(
                        dto.getModule()
                )
        );

        entity.setFonctionnaliteParent(
                fromFonctionnaliteDTO(
                        dto.getFonctionnaliteParent()
                )
        );

        return entity;
    }

    // =========================================================
    // ==================== PointDeVente ========================
    // =========================================================

    public PointDeVenteDTO fromPointDeVente(
            PointDeVente entity) {

        if (entity == null) {
            return null;
        }

        PointDeVenteDTO dto =
                new PointDeVenteDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        dto.setRestaurant(
                fromRestaurant(
                        entity.getRestaurant()
                )
        );

        return dto;
    }

    public PointDeVente fromPointDeVenteDTO(
            PointDeVenteDTO dto) {

        if (dto == null) {
            return null;
        }

        PointDeVente entity =
                new PointDeVente();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setRestaurant(
                fromRestaurantDTO(
                        dto.getRestaurant()
                )
        );

        return entity;
    }

    // =========================================================
    // ==================== Employee ============================
    // =========================================================

    public EmployeeDTO fromEmployee(
            Employee entity) {

        if (entity == null) {
            return null;
        }

        EmployeeDTO dto =
                new EmployeeDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        dto.setRole(
                fromRole(
                        entity.getRole()
                )
        );

        dto.setPdvAffecte(
                fromPointDeVente(
                        entity.getPdvAffecte()
                )
        );
        if (entity.getRestaurant() != null) {
            dto.setRestaurant(
                    fromRestaurant(
                            entity.getRestaurant()
                    )
            );
        }

        return dto;
    }

    public Employee fromEmployeeDTO(
            EmployeeDTO dto) {

        if (dto == null) {
            return null;
        }

        Employee entity =
                new Employee();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setRole(
                fromRoleDTO(
                        dto.getRole()
                )
        );

        entity.setPdvAffecte(
                fromPointDeVenteDTO(
                        dto.getPdvAffecte()
                )
        );
        if (dto.getRestaurant() != null) {
            entity.setRestaurant(
                    fromRestaurantDTO(dto.getRestaurant())
            );
        }

        return entity;
    }

    // =========================================================
    // ==================== Employee Shallow ===================
    // =========================================================

    private EmployeeDTO fromEmployeeShallow(
            Employee entity) {

        if (entity == null) {
            return null;
        }

        EmployeeDTO dto =
                new EmployeeDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        return dto;
    }

    private Employee fromEmployeeDTOShallow(
            EmployeeDTO dto) {

        if (dto == null) {
            return null;
        }

        Employee entity =
                new Employee();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        return entity;
    }

    // =========================================================
    // ==================== Role ================================
    // =========================================================

    public RoleDTO fromRole(
            Role entity) {

        if (entity == null) {
            return null;
        }

        RoleDTO dto =
                new RoleDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        dto.setAttribuePar(
                fromEmployeeShallow(
                        entity.getAttribuePar()
                )
        );

        return dto;
    }

    public Role fromRoleDTO(
            RoleDTO dto) {

        if (dto == null) {
            return null;
        }

        Role entity =
                new Role();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setAttribuePar(
                fromEmployeeDTOShallow(
                        dto.getAttribuePar()
                )
        );

        return entity;
    }

    // =========================================================
    // ==================== RoleFonctionnalite =================
    // =========================================================

    public RoleFonctionnaliteDTO fromRoleFonctionnalite(
            RoleFonctionnalite entity) {

        if (entity == null) {
            return null;
        }

        RoleFonctionnaliteDTO dto =
                new RoleFonctionnaliteDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        dto.setRole(
                fromRole(
                        entity.getRole()
                )
        );

        dto.setFonctionnalite(
                fromFonctionnalite(
                        entity.getFonctionnalite()
                )
        );

        dto.setAttribuePar(
                fromEmployee(
                        entity.getAttribuePar()
                )
        );

        return dto;
    }

    public RoleFonctionnalite fromRoleFonctionnaliteDTO(
            RoleFonctionnaliteDTO dto) {

        if (dto == null) {
            return null;
        }

        RoleFonctionnalite entity =
                new RoleFonctionnalite();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setRole(
                fromRoleDTO(
                        dto.getRole()
                )
        );

        entity.setFonctionnalite(
                fromFonctionnaliteDTO(
                        dto.getFonctionnalite()
                )
        );

        entity.setAttribuePar(
                fromEmployeeDTO(
                        dto.getAttribuePar()
                )
        );

        return entity;
    }

    // =========================================================
    // ==================== TableRestaurant ====================
    // =========================================================

    public TableRestaurantDTO fromTableRestaurant(
            TableRestaurant entity) {

        if (entity == null) {
            return null;
        }

        TableRestaurantDTO dto =
                new TableRestaurantDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        dto.setRestaurant(
                fromRestaurant(
                        entity.getRestaurant()
                )
        );

        dto.setServeurAttribue(
                fromEmployee(
                        entity.getServeurAttribue()
                )
        );

        dto.setGenerePar(
                fromEmployee(
                        entity.getGenerePar()
                )
        );

        return dto;
    }

    public TableRestaurant fromTableRestaurantDTO(
            TableRestaurantDTO dto) {

        if (dto == null) {
            return null;
        }

        TableRestaurant entity =
                new TableRestaurant();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setRestaurant(
                fromRestaurantDTO(
                        dto.getRestaurant()
                )
        );

        entity.setServeurAttribue(
                fromEmployeeDTO(
                        dto.getServeurAttribue()
                )
        );

        entity.setGenerePar(
                fromEmployeeDTO(
                        dto.getGenerePar()
                )
        );

        return entity;
    }

    // =========================================================
    // ================= PriseEnChargeTable ====================
    // =========================================================

    public PriseEnChargeTableDTO fromPriseEnChargeTable(
            PriseEnChargeTable entity) {

        if (entity == null) {
            return null;
        }

        PriseEnChargeTableDTO dto =
                new PriseEnChargeTableDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        dto.setTable(
                fromTableRestaurant(
                        entity.getTable()
                )
        );

        dto.setEmployee(
                fromEmployee(
                        entity.getEmployee()
                )
        );

        return dto;
    }

    public PriseEnChargeTable fromPriseEnChargeTableDTO(
            PriseEnChargeTableDTO dto) {

        if (dto == null) {
            return null;
        }

        PriseEnChargeTable entity =
                new PriseEnChargeTable();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setTable(
                fromTableRestaurantDTO(
                        dto.getTable()
                )
        );

        entity.setEmployee(
                fromEmployeeDTO(
                        dto.getEmployee()
                )
        );

        return entity;
    }

    // =========================================================
    // ==================== ClientAuthentifie ==================
    // =========================================================

    public ClientAuthentifieDTO fromClientAuthentifie(
            ClientAuthentifie entity) {

        if (entity == null) {
            return null;
        }

        ClientAuthentifieDTO dto =
                new ClientAuthentifieDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        // BeanUtils.copyProperties ignore silencieusement "restaurant"
        // (types RestaurantDTO / Restaurant incompatibles) : mapping
        // explicite requis, comme pour Fournisseur/Ingredient.
        dto.setRestaurant(
                fromRestaurant(
                        entity.getRestaurant()
                )
        );

        return dto;
    }

    public ClientAuthentifie fromClientAuthentifieDTO(
            ClientAuthentifieDTO dto) {

        if (dto == null) {
            return null;
        }

        ClientAuthentifie entity =
                new ClientAuthentifie();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setRestaurant(
                fromRestaurantDTO(
                        dto.getRestaurant()
                )
        );

        return entity;
    }

    // =========================================================
    // ==================== ClientNonAuthentifie ===============
    // =========================================================

    public ClientNonAuthentifieDTO fromClientNonAuthentifie(
            ClientNonAuthentifie entity) {

        if (entity == null) {
            return null;
        }

        ClientNonAuthentifieDTO dto =
                new ClientNonAuthentifieDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        dto.setRestaurant(
                fromRestaurant(
                        entity.getRestaurant()
                )
        );

        dto.setTableScannee(
                fromTableRestaurant(
                        entity.getTableScannee()
                )
        );

        return dto;
    }

    public ClientNonAuthentifie fromClientNonAuthentifieDTO(
            ClientNonAuthentifieDTO dto) {

        if (dto == null) {
            return null;
        }

        ClientNonAuthentifie entity =
                new ClientNonAuthentifie();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setRestaurant(
                fromRestaurantDTO(
                        dto.getRestaurant()
                )
        );

        entity.setTableScannee(
                fromTableRestaurantDTO(
                        dto.getTableScannee()
                )
        );

        return entity;
    }

    // =========================================================
    // ==================== Commande ============================
    // =========================================================

    public CommandeDTO fromCommande(
            Commande entity) {

        if (entity == null) {
            return null;
        }

        CommandeDTO dto =
                new CommandeDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        dto.setClient(
                fromClientAuthentifie(
                        entity.getClient()
                )
        );

        dto.setClientNonAuthentifie(
                fromClientNonAuthentifie(
                        entity.getClientNonAuthentifie()
                )
        );

        dto.setTable(
                fromTableRestaurant(
                        entity.getTable()
                )
        );

        dto.setEmployee(
                fromEmployee(
                        entity.getEmployee()
                )
        );

        return dto;
    }

    public Commande fromCommandeDTO(
            CommandeDTO dto) {

        if (dto == null) {
            return null;
        }

        Commande entity =
                new Commande();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setClient(
                fromClientAuthentifieDTO(
                        dto.getClient()
                )
        );

        entity.setClientNonAuthentifie(
                fromClientNonAuthentifieDTO(
                        dto.getClientNonAuthentifie()
                )
        );

        entity.setTable(
                fromTableRestaurantDTO(
                        dto.getTable()
                )
        );

        entity.setEmployee(
                fromEmployeeDTO(
                        dto.getEmployee()
                )
        );

        return entity;
    }

    // =========================================================
    // ==================== LigneCommande ======================
    // =========================================================

    public LigneCommandeDTO fromLigneCommande(
            LigneCommande entity) {

        if (entity == null) {
            return null;
        }

        LigneCommandeDTO dto =
                new LigneCommandeDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        dto.setCommande(
                fromCommande(
                        entity.getCommande()
                )
        );

        dto.setProduit(
                fromProduit(
                        entity.getProduit()
                )
        );

        if (entity.getModificateursSelectionnes() != null) {

            dto.setModificateursSelectionnes(
                    entity.getModificateursSelectionnes()
                            .stream()
                            .map(this::fromLigneCommandeModificateur)
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }

    public LigneCommande fromLigneCommandeDTO(
            LigneCommandeDTO dto) {

        if (dto == null) {
            return null;
        }

        LigneCommande entity =
                new LigneCommande();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setCommande(
                fromCommandeDTO(
                        dto.getCommande()
                )
        );

        entity.setProduit(
                fromProduitDTO(
                        dto.getProduit()
                )
        );

        return entity;
    }

    // =========================================================
    // ==================== Modificateur =======================
    // =========================================================

    public ModificateurDTO fromModificateur(
            Modificateur entity) {

        if (entity == null) {
            return null;
        }

        ModificateurDTO dto =
                new ModificateurDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        if (entity.getProduits() != null) {

            dto.setProduits(
                    entity.getProduits()
                            .stream()
                            .map(this::fromProduit)
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }

    public Modificateur fromModificateurDTO(
            ModificateurDTO dto) {

        if (dto == null) {
            return null;
        }

        Modificateur entity =
                new Modificateur();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        if (dto.getProduits() != null) {

            entity.setProduits(
                    dto.getProduits()
                            .stream()
                            .map(this::fromProduitDTO)
                            .collect(Collectors.toList())
            );
        }

        return entity;
    }

    // =========================================================
    // ============= LigneCommandeModificateur ==================
    // =========================================================

    public LigneCommandeModificateurDTO fromLigneCommandeModificateur(
            LigneCommandeModificateur entity) {

        if (entity == null) {
            return null;
        }

        LigneCommandeModificateurDTO dto =
                new LigneCommandeModificateurDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        // Pas de dto.setLigneCommande(...) ici : on évite la boucle
        // LigneCommande -> modificateursSelectionnes -> ligneCommande -> ...

        dto.setModificateur(
                fromModificateur(
                        entity.getModificateur()
                )
        );

        return dto;
    }

    public LigneCommandeModificateur fromLigneCommandeModificateurDTO(
            LigneCommandeModificateurDTO dto) {

        if (dto == null) {
            return null;
        }

        LigneCommandeModificateur entity =
                new LigneCommandeModificateur();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setLigneCommande(
                fromLigneCommandeDTO(
                        dto.getLigneCommande()
                )
        );

        entity.setModificateur(
                fromModificateurDTO(
                        dto.getModificateur()
                )
        );

        return entity;
    }

    // =========================================================
    // ==================== ModeleRecu =========================
    // =========================================================

    public ModeleRecuDTO fromModeleRecu(
            ModeleRecu entity) {

        if (entity == null) {
            return null;
        }

        ModeleRecuDTO dto =
                new ModeleRecuDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        dto.setRestaurant(
                fromRestaurant(
                        entity.getRestaurant()
                )
        );

        return dto;
    }

    public ModeleRecu fromModeleRecuDTO(
            ModeleRecuDTO dto) {

        if (dto == null) {
            return null;
        }

        ModeleRecu entity =
                new ModeleRecu();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setRestaurant(
                fromRestaurantDTO(
                        dto.getRestaurant()
                )
        );

        return entity;
    }

    // =========================================================
    // ==================== ParametreCompte ====================
    // =========================================================

    public ParametreCompteDTO fromParametreCompte(
            ParametreCompte entity) {

        if (entity == null) {
            return null;
        }

        ParametreCompteDTO dto =
                new ParametreCompteDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        dto.setRestaurant(
                fromRestaurant(
                        entity.getRestaurant()
                )
        );

        return dto;
    }

    public ParametreCompte fromParametreCompteDTO(
            ParametreCompteDTO dto) {

        if (dto == null) {
            return null;
        }

        ParametreCompte entity =
                new ParametreCompte();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setRestaurant(
                fromRestaurantDTO(
                        dto.getRestaurant()
                )
        );

        return entity;
    }

    // =========================================================
    // ==================== PlanDeSalle =========================
    // =========================================================

    public PlanDeSalleDTO fromPlanDeSalle(
            PlanDeSalle entity) {

        if (entity == null) {
            return null;
        }

        PlanDeSalleDTO dto =
                new PlanDeSalleDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        dto.setRestaurant(
                fromRestaurant(
                        entity.getRestaurant()
                )
        );

        return dto;
    }

    public PlanDeSalle fromPlanDeSalleDTO(
            PlanDeSalleDTO dto) {

        if (dto == null) {
            return null;
        }

        PlanDeSalle entity =
                new PlanDeSalle();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setRestaurant(
                fromRestaurantDTO(
                        dto.getRestaurant()
                )
        );

        return entity;
    }

    // =========================================================
    // ==================== Notification =======================
    // =========================================================

    public NotificationDTO fromNotification(
            Notification entity) {

        if (entity == null) {
            return null;
        }

        NotificationDTO dto =
                new NotificationDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        dto.setDestinataire(
                fromUtilisateur(
                        entity.getDestinataire()
                )
        );

        dto.setEmetteur(
                fromUtilisateur(
                        entity.getEmetteur()
                )
        );

        return dto;
    }

    public Notification fromNotificationDTO(
            NotificationDTO dto) {

        if (dto == null) {
            return null;
        }

        Notification entity =
                new Notification();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setDestinataire(
                fromUtilisateurDTO(
                        dto.getDestinataire()
                )
        );

        entity.setEmetteur(
                fromUtilisateurDTO(
                        dto.getEmetteur()
                )
        );

        return entity;
    }

    // =========================================================
    // ==================== Reclamation ========================
    // =========================================================

    public ReclamationDTO fromReclamation(
            Reclamation entity) {

        if (entity == null) {
            return null;
        }

        ReclamationDTO dto =
                new ReclamationDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        dto.setClient(
                fromClientAuthentifie(
                        entity.getClient()
                )
        );

        dto.setCommande(
                fromCommande(
                        entity.getCommande()
                )
        );

        return dto;
    }

    public Reclamation fromReclamationDTO(
            ReclamationDTO dto) {

        if (dto == null) {
            return null;
        }

        Reclamation entity =
                new Reclamation();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setClient(
                fromClientAuthentifieDTO(
                        dto.getClient()
                )
        );

        entity.setCommande(
                fromCommandeDTO(
                        dto.getCommande()
                )
        );

        return entity;
    }

    // =========================================================
    // ==================== Reservation =========================
    // =========================================================

    public ReservationDTO fromReservation(
            Reservation entity) {

        if (entity == null) {
            return null;
        }

        ReservationDTO dto =
                new ReservationDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        dto.setClient(
                fromClientAuthentifie(
                        entity.getClient()
                )
        );

        dto.setRestaurant(
                fromRestaurant(
                        entity.getRestaurant()
                )
        );

        dto.setTable(
                fromTableRestaurant(
                        entity.getTable()
                )
        );

        dto.setConfirmePar(
                fromEmployee(
                        entity.getConfirmePar()
                )
        );

        return dto;
    }

    public Reservation fromReservationDTO(
            ReservationDTO dto) {

        if (dto == null) {
            return null;
        }

        Reservation entity =
                new Reservation();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setClient(
                fromClientAuthentifieDTO(
                        dto.getClient()
                )
        );

        entity.setRestaurant(
                fromRestaurantDTO(
                        dto.getRestaurant()
                )
        );

        entity.setTable(
                fromTableRestaurantDTO(
                        dto.getTable()
                )
        );

        entity.setConfirmePar(
                fromEmployeeDTO(
                        dto.getConfirmePar()
                )
        );

        return entity;
    }

    // =========================================================
    // ==================== Suggestion ==========================
    // =========================================================

    public SuggestionDTO fromSuggestion(
            Suggestion entity) {

        if (entity == null) {
            return null;
        }

        SuggestionDTO dto =
                new SuggestionDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        dto.setClient(
                fromClientAuthentifie(
                        entity.getClient()
                )
        );

        dto.setRestaurant(
                fromRestaurant(
                        entity.getRestaurant()
                )
        );

        return dto;
    }

    public Suggestion fromSuggestionDTO(
            SuggestionDTO dto) {

        if (dto == null) {
            return null;
        }

        Suggestion entity =
                new Suggestion();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setClient(
                fromClientAuthentifieDTO(
                        dto.getClient()
                )
        );

        entity.setRestaurant(
                fromRestaurantDTO(
                        dto.getRestaurant()
                )
        );

        return entity;
    }


    public SessionCaisseDTO fromSessionCaisse(
            SessionCaisse entity) {

        if (entity == null) {
            return null;
        }

        SessionCaisseDTO dto =
                new SessionCaisseDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        dto.setPointDeVente(
                fromPointDeVente(
                        entity.getPointDeVente()
                )
        );

        dto.setEmployee(
                fromEmployee(
                        entity.getEmployee()
                )
        );

        return dto;
    }

    public SessionCaisse fromSessionCaisseDTO(
            SessionCaisseDTO dto) {

        if (dto == null) {
            return null;
        }

        SessionCaisse entity =
                new SessionCaisse();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setPointDeVente(
                fromPointDeVenteDTO(
                        dto.getPointDeVente()
                )
        );

        entity.setEmployee(
                fromEmployeeDTO(
                        dto.getEmployee()
                )
        );

        return entity;
    }

    // =========================================================
    // ==================== VENTE ===============================
    // =========================================================

    public VenteDTO fromVente(Vente vente) {

        if (vente == null) {
            return null;
        }

        VenteDTO dto = new VenteDTO();

        // -------------------------
        // Champs simples
        // -------------------------

        dto.setId_vente(
                vente.getId_vente()
        );

        dto.setDateVente(
                vente.getDateVente()
        );

        dto.setMontantHt(
                vente.getMontantHt()
        );

        dto.setMontantTtc(
                vente.getMontantTtc()
        );

        dto.setMontantReduction(
                vente.getMontantReduction()
        );

        // -------------------------
        // Réduction
        // -------------------------

        dto.setReduction(
                fromReduction(
                        vente.getReduction()
                )
        );

        // -------------------------
        // Commande
        // -------------------------

        dto.setCommande(
                fromCommande(
                        vente.getCommande()
                )
        );

        // -------------------------
        // Point de vente
        // -------------------------

        dto.setPointDeVente(
                fromPointDeVente(
                        vente.getPointDeVente()
                )
        );

        // -------------------------
        // Employé
        // -------------------------

        dto.setEmployee(
                fromEmployee(
                        vente.getEmployee()
                )
        );

        // -------------------------
        // Mode de paiement
        // -------------------------

        dto.setModePaiement(
                fromModePaiement(
                        vente.getModePaiement()
                )
        );

        /*
         * IMPORTANT :
         *
         * On ne mappe PAS vente.getRecu()
         * ici.
         *
         * La relation est :
         *
         * Vente -> Recu -> Vente
         *
         * Mapper les deux côtés provoquerait une
         * récursion infinie.
         *
         * Le reçu est récupéré avec son propre endpoint.
         */

        return dto;
    }

    public Vente fromVenteDTO(VenteDTO dto) {

        if (dto == null) {
            return null;
        }

        Vente entity = new Vente();

        // -------------------------
        // Champs simples
        // -------------------------

        entity.setId_vente(
                dto.getId_vente()
        );

        entity.setDateVente(
                dto.getDateVente()
        );

        entity.setMontantHt(
                dto.getMontantHt()
        );

        entity.setMontantTtc(
                dto.getMontantTtc()
        );

        entity.setMontantReduction(
                dto.getMontantReduction()
        );

        // -------------------------
        // Réduction
        // -------------------------

        if (dto.getReduction() != null) {

            entity.setReduction(
                    fromReductionDTO(
                            dto.getReduction()
                    )
            );
        }

        // -------------------------
        // Commande
        // -------------------------

        if (dto.getCommande() != null) {

            entity.setCommande(
                    fromCommandeDTO(
                            dto.getCommande()
                    )
            );
        }

        // -------------------------
        // Point de vente
        // -------------------------

        if (dto.getPointDeVente() != null) {

            entity.setPointDeVente(
                    fromPointDeVenteDTO(
                            dto.getPointDeVente()
                    )
            );
        }

        // -------------------------
        // Employé
        // -------------------------

        if (dto.getEmployee() != null) {

            entity.setEmployee(
                    fromEmployeeDTO(
                            dto.getEmployee()
                    )
            );
        }

        // -------------------------
        // Mode de paiement
        // -------------------------

        if (dto.getModePaiement() != null) {

            entity.setModePaiement(
                    fromModePaiementDTO(
                            dto.getModePaiement()
                    )
            );
        }

        /*
         * IMPORTANT :
         *
         * Le Recu n'est volontairement pas mappé ici.
         *
         * Vente <-> Recu est une relation OneToOne
         * gérée séparément.
         */

        return entity;
    }

    // =========================================================
    // ==================== Recu ================================
    // =========================================================

    public RecuDTO fromRecu(Recu entity) {

        if (entity == null) {
            return null;
        }

        RecuDTO dto = new RecuDTO();

        // -------------------------
        // Champs simples
        // -------------------------

        dto.setId_recu(
                entity.getId_recu()
        );

        dto.setNumeroRecu(
                entity.getNumeroRecu()
        );

        dto.setDate_emission(
                entity.getDate_emission()
        );

        dto.setLogo_affiche(
                entity.getLogo_affiche()
        );

        dto.setEntete_personnalise(
                entity.getEntete_personnalise()
        );

        dto.setPied_de_page_personnalise(
                entity.getPied_de_page_personnalise()
        );

        dto.setCommentaire_client(
                entity.getCommentaire_client()
        );

        // -------------------------
        // Vente
        // -------------------------

        /*
         * On conserve volontairement :
         *
         * RecuDTO.vente = VenteDTO
         *
         * fromVente() ne mappe pas le Recu,
         * donc aucune récursion.
         */

        dto.setVente(
                fromVente(
                        entity.getVente()
                )
        );

        // -------------------------
        // Modèle de reçu
        // -------------------------

        dto.setModele(
                fromModeleRecu(
                        entity.getModele()
                )
        );

        return dto;
    }

    public Recu fromRecuDTO(RecuDTO dto) {

        if (dto == null) {
            return null;
        }

        Recu entity = new Recu();

        // -------------------------
        // Champs simples
        // -------------------------

        entity.setId_recu(
                dto.getId_recu()
        );

        entity.setNumeroRecu(
                dto.getNumeroRecu()
        );

        entity.setDate_emission(
                dto.getDate_emission()
        );

        entity.setLogo_affiche(
                dto.getLogo_affiche()
        );

        entity.setEntete_personnalise(
                dto.getEntete_personnalise()
        );

        entity.setPied_de_page_personnalise(
                dto.getPied_de_page_personnalise()
        );

        entity.setCommentaire_client(
                dto.getCommentaire_client()
        );

        // -------------------------
        // Vente
        // -------------------------

        /*
         * On conserve la référence directe :
         *
         * RecuDTO -> VenteDTO
         *
         * et :
         *
         * VenteDTO -> Vente
         *
         * sans remapper le Recu depuis Vente.
         */

        if (dto.getVente() != null) {

            entity.setVente(
                    fromVenteDTO(
                            dto.getVente()
                    )
            );
        }

        // -------------------------
        // Modèle de reçu
        // -------------------------

        if (dto.getModele() != null) {

            entity.setModele(
                    fromModeleRecuDTO(
                            dto.getModele()
                    )
            );
        }

        return entity;
    }

    // =========================================================
    // ==================== Penalite ============================
    // =========================================================

    public PenaliteDTO fromPenalite(
            Penalite entity) {

        if (entity == null) {
            return null;
        }

        PenaliteDTO dto =
                new PenaliteDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        dto.setEmployee(
                fromEmployee(
                        entity.getEmployee()
                )
        );

        return dto;
    }

    public Penalite fromPenaliteDTO(
            PenaliteDTO dto) {

        if (dto == null) {
            return null;
        }

        Penalite entity =
                new Penalite();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setEmployee(
                fromEmployeeDTO(
                        dto.getEmployee()
                )
        );

        return entity;
    }

    // =========================================================
    // ==================== Performance =========================
    // =========================================================

    public PerformanceDTO fromPerformance(
            Performance entity) {

        if (entity == null) {
            return null;
        }

        PerformanceDTO dto =
                new PerformanceDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        dto.setEmployee(
                fromEmployee(
                        entity.getEmployee()
                )
        );

        return dto;
    }

    public Performance fromPerformanceDTO(
            PerformanceDTO dto) {

        if (dto == null) {
            return null;
        }

        Performance entity =
                new Performance();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setEmployee(
                fromEmployeeDTO(
                        dto.getEmployee()
                )
        );

        return entity;
    }

    // =========================================================
    // ==================== Presence ============================
    // =========================================================

    public PresenceDTO fromPresence(
            Presence entity) {

        if (entity == null) {
            return null;
        }

        PresenceDTO dto =
                new PresenceDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        dto.setEmployee(
                fromEmployee(
                        entity.getEmployee()
                )
        );

        return dto;
    }

    public Presence fromPresenceDTO(
            PresenceDTO dto) {

        if (dto == null) {
            return null;
        }

        Presence entity =
                new Presence();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setEmployee(
                fromEmployeeDTO(
                        dto.getEmployee()
                )
        );

        return entity;
    }

    // =========================================================
    // ==================== VersementSalaire ====================
    // =========================================================

    public VersementSalaireDTO fromVersementSalaire(
            VersementSalaire entity) {

        if (entity == null) {
            return null;
        }

        VersementSalaireDTO dto =
                new VersementSalaireDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        dto.setEmployee(
                fromEmployee(
                        entity.getEmployee()
                )
        );

        return dto;
    }

    public VersementSalaire fromVersementSalaireDTO(
            VersementSalaireDTO dto) {

        if (dto == null) {
            return null;
        }

        VersementSalaire entity =
                new VersementSalaire();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setEmployee(
                fromEmployeeDTO(
                        dto.getEmployee()
                )
        );

        return entity;
    }

    // =========================================================
    // ==================== PrevisionStock ======================
    // =========================================================

    public PrevisionStockDTO fromPrevisionStock(
            PrevisionStock entity) {

        if (entity == null) {
            return null;
        }

        PrevisionStockDTO dto =
                new PrevisionStockDTO();

        BeanUtils.copyProperties(
                entity,
                dto
        );

        dto.setIngredient(
                fromIngredient(
                        entity.getIngredient()
                )
        );

        return dto;
    }

    public PrevisionStock fromPrevisionStockDTO(
            PrevisionStockDTO dto) {

        if (dto == null) {
            return null;
        }

        PrevisionStock entity =
                new PrevisionStock();

        BeanUtils.copyProperties(
                dto,
                entity
        );

        entity.setIngredient(
                fromIngredientDTO(
                        dto.getIngredient()
                )
        );

        return entity;
    }

    // =========================================================
    // ==================== UPDATE ==============================
    // =========================================================

    public void updateUtilisateurFromDto(
            UtilisateurDTO dto,
            Utilisateur entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(dto, "id")
        );
    }

    public void updateRestaurantFromDto(
            RestaurantDTO dto,
            Restaurant entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(dto, "id")
        );
    }

    public void updateFournisseurFromDto(
            FournisseurDTO dto,
            Fournisseur entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(dto, "id")
        );

        if (dto.getRestaurant() != null) {
            entity.setRestaurant(
                    fromRestaurantDTO(
                            dto.getRestaurant()
                    )
            );
        }
    }

    // =========================================================
    // ==================== UPDATE MODE PAIEMENT ===============
    // =========================================================

    public void updateModePaiementFromDto(
            ModePaiementDTO dto,
            ModePaiement entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(
                        dto,
                        "id_mode_paiement"
                )
        );

        if (dto.getClient() != null) {

            entity.setClient(
                    fromClientAuthentifieDTO(
                            dto.getClient()
                    )
            );
        }
    }

    // =========================================================
    // ==================== UPDATE MODULE =======================
    // =========================================================

    public void updateModuleFromDto(
            ModuleDTO dto,
            Module entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(dto, "id")
        );
    }

    // =========================================================
    // ==================== UPDATE REDUCTION ====================
    // =========================================================

    public void updateReductionFromDto(
            ReductionDTO dto,
            Reduction entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(
                        dto,
                        "id_reduction",
                        // Compteur géré exclusivement par le backend
                        // (incrémenté à chaque application réelle de la
                        // réduction) : ne jamais l'écraser avec la valeur
                        // envoyée par le front.
                        "nombreApplicationsEffectuees"
                )
        );

        if (dto.getType() != null
                && !dto.getType().isBlank()) {

            try {

                entity.setType(
                        TypeReduction.valueOf(
                                dto.getType()
                                        .trim()
                                        .toUpperCase()
                        )
                );

            } catch (IllegalArgumentException e) {

                throw new IllegalArgumentException(
                        "Type de réduction invalide : "
                                + dto.getType()
                                + ". Valeurs acceptées : POURCENTAGE ou MONTANT_FIXE."
                );
            }
        }

        if (dto.getApplicationProduits() != null
                && !dto.getApplicationProduits().isBlank()) {

            try {
                entity.setApplicationProduits(
                        ApplicationReduction.valueOf(
                                dto.getApplicationProduits().trim().toUpperCase()
                        )
                );
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Application de réduction invalide : "
                                + dto.getApplicationProduits()
                                + ". Valeurs acceptées : TOUS_PRODUITS ou PRODUITS_SPECIFIQUES."
                );
            }
        }

        // montantMinimum : on autorise explicitement de le remettre à null
        // (= "n'importe quel montant"), donc on recopie toujours la valeur.
        entity.setMontantMinimum(
                dto.getMontantMinimum()
        );

        // automatique : on ne touche pas au flag existant si le front
        // n'envoie rien, pour éviter de désactiver silencieusement
        // l'application automatique.
        if (dto.getAutomatique() != null) {
            entity.setAutomatique(dto.getAutomatique());
        }

        // active : idem, on ne touche pas au flag existant si le front
        // n'envoie rien (bouton dédié activer/désactiver, voir
        // ReductionController /activer et /desactiver).
        if (dto.getActive() != null) {
            entity.setActive(dto.getActive());
        }

        // nombreApplicationsAutorise : on autorise explicitement de le
        // remettre à null (= "illimité"), donc on recopie toujours la
        // valeur, comme pour montantMinimum ci-dessus.
        entity.setNombreApplicationsAutorise(
                dto.getNombreApplicationsAutorise()
        );

        // NB : la liste des produits (dto.getProduitsIds()) est résolue et
        // affectée dans ReductionServiceImpl, qui a accès au ProduitRepository.

        // restaurant : mapping explicite (comme fromReduction/fromReductionDTO
        // ci-dessus) — BeanUtils.copyProperties ignore silencieusement ce
        // champ (types RestaurantDTO / Restaurant incompatibles). Le
        // contrôleur réaffecte toujours le restaurant existant avant
        // d'appeler cette méthode, donc dto.getRestaurant() ne peut pas
        // "déplacer" la réduction vers un autre restaurant.
        if (dto.getRestaurant() != null) {
            entity.setRestaurant(
                    fromRestaurantDTO(
                            dto.getRestaurant()
                    )
            );
        }
    }

    // =========================================================
    // ==================== UPDATE TAXE =========================
    // =========================================================

    public void updateTaxeFromDto(
            TaxeDTO dto,
            Taxe entity) {

        if (dto == null || entity == null) {
            return;
        }

        // Mapping explicite (voir fromTaxe / fromTaxeDTO ci-dessus) : le
        // mismatch camelCase / snake_case faisait que BeanUtils ne copiait
        // jamais nomTaxe, applicableA ni dateCreation, donc les
        // modifications n'étaient jamais réellement enregistrées.

        if (dto.getNom_taxe() != null) {
            entity.setNomTaxe(dto.getNom_taxe());
        }

        if (dto.getTaux() != null) {
            entity.setTaux(dto.getTaux());
        }

        if (dto.getApplicable_a() != null) {
            entity.setApplicableA(dto.getApplicable_a());
        }

        if (dto.getStatut() != null) {
            entity.setStatut(dto.getStatut());
        }

        // id_taxe et date_creation ne sont volontairement pas modifiables
        // via une mise à jour.
    }

    // =========================================================
    // ==================== UPDATE BON COMMANDE =================
    // =========================================================

    public void updateBonDeCommandeStockFromDto(
            BonDeCommandeStockDTO dto,
            BonDeCommandeStock entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(
                        dto,
                        "id_bon_commande"
                )
        );

        if (dto.getFournisseur() != null) {

            entity.setFournisseur(
                    fromFournisseurDTO(
                            dto.getFournisseur()
                    )
            );
        }

        if (dto.getRestaurant() != null) {
            entity.setRestaurant(
                    fromRestaurantDTO(
                            dto.getRestaurant()
                    )
            );
        }
    }

    // =========================================================
    // ==================== UPDATE ELEMENT MENU =================
    // =========================================================

    public void updateElementMenuFromDto(
            ElementMenuDTO dto,
            ElementMenu entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(dto, "id")
        );

        if (dto.getCategorieParent() != null) {

            entity.setCategorieParent(
                    fromCategorieDTO(
                            dto.getCategorieParent()
                    )
            );
        }

        if (dto.getRestaurant() != null) {

            entity.setRestaurant(
                    fromRestaurantDTO(
                            dto.getRestaurant()
                    )
            );
        }
    }

    // =========================================================
    // ==================== UPDATE CATEGORIE ====================
    // =========================================================

    public void updateCategorieFromDto(
            CategorieDTO dto,
            Categorie entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(dto, "id")
        );

        if (dto.getCategorieParent() != null) {

            entity.setCategorieParent(
                    fromCategorieDTO(
                            dto.getCategorieParent()
                    )
            );
        }

        if (dto.getRestaurant() != null) {

            entity.setRestaurant(
                    fromRestaurantDTO(
                            dto.getRestaurant()
                    )
            );
        }
    }

    // =========================================================
    // ==================== UPDATE PRODUIT ======================
    // =========================================================
    public void updateProduitFromDto(
            ProduitDTO dto,
            Produit entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(
                        dto,
                        "id_element"
                )
        );

        if (dto.getCategorieParent() != null) {
            entity.setCategorieParent(
                    fromCategorieDTO(
                            dto.getCategorieParent()
                    )
            );
        } else {
            entity.setCategorieParent(null);
        }

        if (dto.getRestaurant() != null) {
            entity.setRestaurant(
                    fromRestaurantDTO(
                            dto.getRestaurant()
                    )
            );
        }

        if (dto.getIngredients() != null) {
            entity.setIngredients(
                    dto.getIngredients()
                            .stream()
                            .map(this::fromIngredientDTO)
                            .collect(Collectors.toList())
            );
        }
    }


    // =========================================================
    // ==================== UPDATE INGREDIENT ===================
    // =========================================================

    public void updateIngredientFromDto(
            IngredientDTO dto,
            Ingredient entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(dto, "id")
        );

        if (dto.getFournisseur() != null) {

            entity.setFournisseur(
                    fromFournisseurDTO(
                            dto.getFournisseur()
                    )
            );
        }

        if (dto.getRestaurant() != null) {
            entity.setRestaurant(
                    fromRestaurantDTO(
                            dto.getRestaurant()
                    )
            );
        }

        if (dto.getProduits() != null) {

            entity.setProduits(
                    dto.getProduits()
                            .stream()
                            .map(this::fromProduitDTOShallow)
                            .collect(Collectors.toList())
            );
        }
    }

    // =========================================================
    // ==================== UPDATE LIGNE BON COMMANDE ==========
    // =========================================================

    public void updateLigneBonDeCommandeFromDto(
            LigneBonDeCommandeDTO dto,
            LigneBonDeCommande entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(dto, "id")
        );

        if (dto.getBonCommande() != null) {

            entity.setBonCommande(
                    fromBonDeCommandeStockDTO(
                            dto.getBonCommande()
                    )
            );
        }

        if (dto.getIngredient() != null) {

            entity.setIngredient(
                    fromIngredientDTO(
                            dto.getIngredient()
                    )
            );
        }
    }

    // =========================================================
    // ==================== UPDATE FONCTIONNALITE ==============
    // =========================================================

    public void updateFonctionnaliteFromDto(
            FonctionnaliteDTO dto,
            Fonctionnalite entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(dto, "id")
        );

        if (dto.getModule() != null) {

            entity.setModule(
                    fromModuleDTO(
                            dto.getModule()
                    )
            );
        }

        if (dto.getFonctionnaliteParent() != null) {

            entity.setFonctionnaliteParent(
                    fromFonctionnaliteDTO(
                            dto.getFonctionnaliteParent()
                    )
            );
        }
    }

    // =========================================================
    // ==================== UPDATE PDV ==========================
    // =========================================================

    public void updatePointDeVenteFromDto(
            PointDeVenteDTO dto,
            PointDeVente entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(
                        dto,
                        "id_pdv"
                )
        );

        if (dto.getRestaurant() != null) {

            entity.setRestaurant(
                    fromRestaurantDTO(
                            dto.getRestaurant()
                    )
            );
        }
    }

    // =========================================================
    // ==================== UPDATE EMPLOYEE =====================
    // =========================================================

    public void updateEmployeeFromDto(
            EmployeeDTO dto,
            Employee entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(dto, "id")
        );

        if (dto.getRole() != null) {

            entity.setRole(
                    fromRoleDTO(
                            dto.getRole()
                    )
            );
        }

        if (dto.getPdvAffecte() != null) {

            entity.setPdvAffecte(
                    fromPointDeVenteDTO(
                            dto.getPdvAffecte()
                    )
            );
        }
    }

    // =========================================================
    // ==================== UPDATE ROLE =========================
    // =========================================================

    public void updateRoleFromDto(
            RoleDTO dto,
            Role entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(dto, "id")
        );

        if (dto.getAttribuePar() != null) {

            entity.setAttribuePar(
                    fromEmployeeDTOShallow(
                            dto.getAttribuePar()
                    )
            );
        }
    }

    // =========================================================
    // ==================== UPDATE ROLE FONCTIONNALITE ==========
    // =========================================================

    public void updateRoleFonctionnaliteFromDto(
            RoleFonctionnaliteDTO dto,
            RoleFonctionnalite entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(dto, "id")
        );

        if (dto.getRole() != null) {

            entity.setRole(
                    fromRoleDTO(
                            dto.getRole()
                    )
            );
        }

        if (dto.getFonctionnalite() != null) {

            entity.setFonctionnalite(
                    fromFonctionnaliteDTO(
                            dto.getFonctionnalite()
                    )
            );
        }

        if (dto.getAttribuePar() != null) {

            entity.setAttribuePar(
                    fromEmployeeDTO(
                            dto.getAttribuePar()
                    )
            );
        }
    }

    // =========================================================
    // ==================== UPDATE TABLE ========================
    // =========================================================

    public void updateTableRestaurantFromDto(
            TableRestaurantDTO dto,
            TableRestaurant entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(dto, "id")
        );

        if (dto.getRestaurant() != null) {

            entity.setRestaurant(
                    fromRestaurantDTO(
                            dto.getRestaurant()
                    )
            );
        }

        if (dto.getServeurAttribue() != null) {

            entity.setServeurAttribue(
                    fromEmployeeDTO(
                            dto.getServeurAttribue()
                    )
            );
        }

        if (dto.getGenerePar() != null) {

            entity.setGenerePar(
                    fromEmployeeDTO(
                            dto.getGenerePar()
                    )
            );
        }
    }

    // =========================================================
    // ==================== UPDATE CLIENT AUTH ==================
    // =========================================================

    public void updateClientAuthentifieFromDto(
            ClientAuthentifieDTO dto,
            ClientAuthentifie entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(dto, "id")
        );

        if (dto.getRestaurant() != null) {

            entity.setRestaurant(
                    fromRestaurantDTO(
                            dto.getRestaurant()
                    )
            );
        }
    }

    // =========================================================
    // ==================== UPDATE CLIENT NON AUTH ==============
    // =========================================================

    public void updateClientNonAuthentifieFromDto(
            ClientNonAuthentifieDTO dto,
            ClientNonAuthentifie entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(dto, "id")
        );

        if (dto.getRestaurant() != null) {

            entity.setRestaurant(
                    fromRestaurantDTO(
                            dto.getRestaurant()
                    )
            );
        }

        if (dto.getTableScannee() != null) {

            entity.setTableScannee(
                    fromTableRestaurantDTO(
                            dto.getTableScannee()
                    )
            );
        }
    }

    // =========================================================
    // ==================== UPDATE COMMANDE =====================
    // =========================================================

    public void updateCommandeFromDto(
            CommandeDTO dto,
            Commande entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(dto, "id")
        );

        if (dto.getClient() != null) {

            entity.setClient(
                    fromClientAuthentifieDTO(
                            dto.getClient()
                    )
            );
        }

        if (dto.getClientNonAuthentifie() != null) {

            entity.setClientNonAuthentifie(
                    fromClientNonAuthentifieDTO(
                            dto.getClientNonAuthentifie()
                    )
            );
        }

        if (dto.getTable() != null) {

            entity.setTable(
                    fromTableRestaurantDTO(
                            dto.getTable()
                    )
            );
        }

        if (dto.getEmployee() != null) {

            entity.setEmployee(
                    fromEmployeeDTO(
                            dto.getEmployee()
                    )
            );
        }
    }

    // =========================================================
    // ==================== UPDATE LIGNE COMMANDE ==============
    // =========================================================

    public void updateLigneCommandeFromDto(
            LigneCommandeDTO dto,
            LigneCommande entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(dto, "id")
        );

        if (dto.getCommande() != null) {

            entity.setCommande(
                    fromCommandeDTO(
                            dto.getCommande()
                    )
            );
        }

        if (dto.getProduit() != null) {

            entity.setProduit(
                    fromProduitDTO(
                            dto.getProduit()
                    )
            );
        }
    }

    // =========================================================
    // ==================== UPDATE MODIFICATEUR ================
    // =========================================================

    public void updateModificateurFromDto(
            ModificateurDTO dto,
            Modificateur entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(dto, "id")
        );

        if (dto.getProduits() != null) {

            entity.setProduits(
                    dto.getProduits()
                            .stream()
                            .map(this::fromProduitDTO)
                            .collect(Collectors.toList())
            );
        }
    }

    // =========================================================
    // ==================== UPDATE MODELE RECU ==================
    // =========================================================

    public void updateModeleRecuFromDto(
            ModeleRecuDTO dto,
            ModeleRecu entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(dto, "id")
        );

        if (dto.getRestaurant() != null) {

            entity.setRestaurant(
                    fromRestaurantDTO(
                            dto.getRestaurant()
                    )
            );
        }
    }

    // =========================================================
    // ==================== UPDATE PARAMETRE COMPTE ==============
    // =========================================================

    public void updateParametreCompteFromDto(
            ParametreCompteDTO dto,
            ParametreCompte entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(dto, "id")
        );

        if (dto.getRestaurant() != null) {

            entity.setRestaurant(
                    fromRestaurantDTO(
                            dto.getRestaurant()
                    )
            );
        }
    }

    // =========================================================
    // ==================== UPDATE PLAN DE SALLE ================
    // =========================================================

    public void updatePlanDeSalleFromDto(
            PlanDeSalleDTO dto,
            PlanDeSalle entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(dto, "id")
        );

        if (dto.getRestaurant() != null) {

            entity.setRestaurant(
                    fromRestaurantDTO(
                            dto.getRestaurant()
                    )
            );
        }
    }

    // =========================================================
    // ==================== UPDATE NOTIFICATION =================
    // =========================================================

    public void updateNotificationFromDto(
            NotificationDTO dto,
            Notification entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(dto, "id")
        );

        if (dto.getDestinataire() != null) {

            entity.setDestinataire(
                    fromUtilisateurDTO(
                            dto.getDestinataire()
                    )
            );
        }

        if (dto.getEmetteur() != null) {

            entity.setEmetteur(
                    fromUtilisateurDTO(
                            dto.getEmetteur()
                    )
            );
        }
    }

    // =========================================================
    // ==================== UPDATE RECLAMATION ==================
    // =========================================================

    public void updateReclamationFromDto(
            ReclamationDTO dto,
            Reclamation entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(dto, "id")
        );

        if (dto.getClient() != null) {

            entity.setClient(
                    fromClientAuthentifieDTO(
                            dto.getClient()
                    )
            );
        }

        if (dto.getCommande() != null) {

            entity.setCommande(
                    fromCommandeDTO(
                            dto.getCommande()
                    )
            );
        }
    }

    // =========================================================
    // ==================== UPDATE RESERVATION ==================
    // =========================================================

    public void updateReservationFromDto(
            ReservationDTO dto,
            Reservation entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(dto, "id")
        );

        if (dto.getClient() != null) {

            entity.setClient(
                    fromClientAuthentifieDTO(
                            dto.getClient()
                    )
            );
        }

        if (dto.getRestaurant() != null) {

            entity.setRestaurant(
                    fromRestaurantDTO(
                            dto.getRestaurant()
                    )
            );
        }

        if (dto.getTable() != null) {

            entity.setTable(
                    fromTableRestaurantDTO(
                            dto.getTable()
                    )
            );
        }

        if (dto.getConfirmePar() != null) {

            entity.setConfirmePar(
                    fromEmployeeDTO(
                            dto.getConfirmePar()
                    )
            );
        }
    }

    // =========================================================
    // ==================== UPDATE SUGGESTION ===================
    // =========================================================

    public void updateSuggestionFromDto(
            SuggestionDTO dto,
            Suggestion entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(dto, "id")
        );

        if (dto.getClient() != null) {

            entity.setClient(
                    fromClientAuthentifieDTO(
                            dto.getClient()
                    )
            );
        }

        if (dto.getRestaurant() != null) {

            entity.setRestaurant(
                    fromRestaurantDTO(
                            dto.getRestaurant()
                    )
            );
        }
    }


    public void updateSessionCaisseFromDto(
            SessionCaisseDTO dto,
            SessionCaisse entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(dto, "id")
        );

        if (dto.getPointDeVente() != null) {

            entity.setPointDeVente(
                    fromPointDeVenteDTO(
                            dto.getPointDeVente()
                    )
            );
        }

        if (dto.getEmployee() != null) {

            entity.setEmployee(
                    fromEmployeeDTO(
                            dto.getEmployee()
                    )
            );
        }
    }

    // =========================================================
    // ==================== UPDATE VENTE ========================
    // =========================================================

    public void updateVenteFromDto(
            VenteDTO dto,
            Vente entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(
                        dto,
                        "id_vente"
                )
        );

        // -------------------------
        // Réduction
        // -------------------------

        if (dto.getReduction() != null) {

            entity.setReduction(
                    fromReductionDTO(
                            dto.getReduction()
                    )
            );

        } else {

            /*
             * Si le DTO contient explicitement null,
             * on retire la réduction.
             */
            entity.setReduction(null);
            entity.setMontantReduction(0.0);
        }

        // -------------------------
        // Commande
        // -------------------------

        if (dto.getCommande() != null) {

            entity.setCommande(
                    fromCommandeDTO(
                            dto.getCommande()
                    )
            );
        }

        // -------------------------
        // Point de vente
        // -------------------------

        if (dto.getPointDeVente() != null) {

            entity.setPointDeVente(
                    fromPointDeVenteDTO(
                            dto.getPointDeVente()
                    )
            );
        }

        // -------------------------
        // Employé
        // -------------------------

        if (dto.getEmployee() != null) {

            entity.setEmployee(
                    fromEmployeeDTO(
                            dto.getEmployee()
                    )
            );
        }

        // -------------------------
        // Mode de paiement
        // -------------------------

        if (dto.getModePaiement() != null) {

            entity.setModePaiement(
                    fromModePaiementDTO(
                            dto.getModePaiement()
                    )
            );
        }

        /*
         * Le Recu n'est pas modifié ici.
         *
         * La relation OneToOne Vente <-> Recu
         * est gérée séparément.
         */
    }

    // =========================================================
    // ==================== UPDATE RECU =========================
    // =========================================================

    public void updateRecuFromDto(
            RecuDTO dto,
            Recu entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(
                        dto,
                        "id_recu"
                )
        );

        // -------------------------
        // Vente
        // -------------------------

        if (dto.getVente() != null) {

            entity.setVente(
                    fromVenteDTO(
                            dto.getVente()
                    )
            );
        }

        // -------------------------
        // Modèle
        // -------------------------

        if (dto.getModele() != null) {

            entity.setModele(
                    fromModeleRecuDTO(
                            dto.getModele()
                    )
            );
        }
    }

    // =========================================================
    // ==================== UPDATE PENALITE =====================
    // =========================================================

    public void updatePenaliteFromDto(
            PenaliteDTO dto,
            Penalite entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(dto, "id")
        );

        if (dto.getEmployee() != null) {

            entity.setEmployee(
                    fromEmployeeDTO(
                            dto.getEmployee()
                    )
            );
        }
    }

    // =========================================================
    // ==================== UPDATE PERFORMANCE =================
    // =========================================================

    public void updatePerformanceFromDto(
            PerformanceDTO dto,
            Performance entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(dto, "id")
        );

        if (dto.getEmployee() != null) {

            entity.setEmployee(
                    fromEmployeeDTO(
                            dto.getEmployee()
                    )
            );
        }
    }

    // =========================================================
    // ==================== UPDATE PRESENCE ====================
    // =========================================================

    public void updatePresenceFromDto(
            PresenceDTO dto,
            Presence entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(dto, "id")
        );

        if (dto.getEmployee() != null) {

            entity.setEmployee(
                    fromEmployeeDTO(
                            dto.getEmployee()
                    )
            );
        }
    }

    // =========================================================
    // ==================== UPDATE VERSEMENT SALAIRE ===========
    // =========================================================

    public void updateVersementSalaireFromDto(
            VersementSalaireDTO dto,
            VersementSalaire entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(dto, "id")
        );

        if (dto.getEmployee() != null) {

            entity.setEmployee(
                    fromEmployeeDTO(
                            dto.getEmployee()
                    )
            );
        }
    }

    // =========================================================
    // ==================== UPDATE PREVISION STOCK ==============
    // =========================================================

    public void updatePrevisionStockFromDto(
            PrevisionStockDTO dto,
            PrevisionStock entity) {

        if (dto == null || entity == null) {
            return;
        }

        BeanUtils.copyProperties(
                dto,
                entity,
                withIgnoredId(dto, "id")
        );

        if (dto.getIngredient() != null) {

            entity.setIngredient(
                    fromIngredientDTO(
                            dto.getIngredient()
                    )
            );
        }
    }

    // =========================================================
    // ==================== UTILITAIRES =========================
    // =========================================================

    public static String[] getNullPropertyNames(
            Object source) {

        if (source == null) {
            return new String[0];
        }

        final BeanWrapper wrappedSource =
                new BeanWrapperImpl(source);

        return Stream.of(
                        wrappedSource.getPropertyDescriptors()
                )
                .map(FeatureDescriptor::getName)
                .filter(
                        propertyName ->
                                wrappedSource.getPropertyValue(
                                        propertyName
                                ) == null
                )
                .toArray(String[]::new);
    }

    private String[] withIgnoredId(
            Object dto,
            String... idFieldNames) {

        String[] nullProps =
                getNullPropertyNames(dto);

        String[] result =
                Arrays.copyOf(
                        nullProps,
                        nullProps.length + idFieldNames.length
                );

        System.arraycopy(
                idFieldNames,
                0,
                result,
                nullProps.length,
                idFieldNames.length
        );

        return result;
    }
}
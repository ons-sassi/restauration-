package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.Ingredient;
import org.sid.restaurationbackend.entities.PrevisionStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PrevisionStockRepository extends JpaRepository<PrevisionStock, Long> {
    List<PrevisionStock> findByIngredient(Ingredient ingredient);
    List<PrevisionStock> findByPeriode(String periode);

    List<PrevisionStock> findByBaseeSurVentes(Boolean baseeSurVentes);
}

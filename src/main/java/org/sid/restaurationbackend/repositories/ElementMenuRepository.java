package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;


@Repository
public interface ElementMenuRepository extends JpaRepository<ElementMenu, Long> {

    Collection<ElementMenu> findByCategorieParent(Categorie Categorie);
}

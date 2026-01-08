package com.aletheia.miniproject.repos;

import com.aletheia.miniproject.core.entities.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IOfferRepository extends JpaRepository<Offer, Long> {
    @Query("""
        select distinct o
        from Offer o
        join o.categories oc
        where oc.category.id = :categoryId
    """)
    List<Offer> findDistinctByCategoriesId(Long categoryId);
}

package com.aletheia.miniproject.repos;

import com.aletheia.miniproject.core.entities.Demand;
import com.aletheia.miniproject.core.entities.DemandStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDemandRepository extends JpaRepository<Demand, Long> {
    @Query("""
        select d
        from Demand d
        where d.offer.id = :offerId
        order by d.createdAt asc
    """)
    List<Demand> findByOfferIdOrderByCreatedAtAsc(@Param("offerId") Long offerId);

    @Query("""
        select d
        from Demand d
        where d.offer.id = :offerId
          and d.status = :status
        order by d.createdAt asc
    """)
    List<Demand> findByOfferIdAndStatusOrderByCreatedAtAsc(@Param("offerId") Long offerId, @Param("status") DemandStatus status);

    @Query("""
        select case when count(d) > 0 then true else false end
        from Demand d
        where d.offer.id = :offerId
          and d.demander.id = :demanderId
          and d.status = com.aletheia.miniproject.core.entities.DemandStatus.PENDING
    """)
    boolean existsPendingByOfferIdAndDemanderId(
            @Param("offerId") Long offerId,
            @Param("demanderId") Long demanderId
    );
}

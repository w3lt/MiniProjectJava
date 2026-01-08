package com.aletheia.miniproject.controllers;

import com.aletheia.miniproject.core.entities.*;
import com.aletheia.miniproject.core.facade.IRessourcerieFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
public class RessourcerieController {

    private final IRessourcerieFacade facade;

    public RessourcerieController(IRessourcerieFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/test")
    public String runScenario() {
        StringBuilder log = new StringBuilder("<pre>");

        log.append("=== RESSOURCERIE E2E TEST ===\n\n");

        try {
            runNominalScenario(log);
            runErrorCases(log);
        } catch (Exception e) {
            log.append("UNEXPECTED ERROR: ").append(e.getMessage()).append("\n");
        }

        log.append("\n=== END ===</pre>");
        return log.toString();
    }

    private void runNominalScenario(StringBuilder log) {
        log.append(">> Creating initial data\n");

        // representer can be null at creation now
        Association association = facade.createAssociation("Emmaüs");
        log.append("Association created: ").append(association.getId()).append(" / ").append(association.getName()).append("\n");

        // member must belong to an association
        Member representer = facade.addMember(association.getId(), "Representer");
        log.append("Member created: ").append(representer.getId()).append(" / ").append(representer.getName()).append("\n");

        Category savedCategory = facade.createCategory("Furniture");
        log.append("Category created: ").append(savedCategory.getId()).append(" / ").append(savedCategory.getName()).append("\n");

        Offer offer = facade.createOffer(
                representer.getId(),
                "Wooden Table",
                "Solid oak",
                BigDecimal.valueOf(50),
                List.of(savedCategory.getId())
        );
        log.append("Offer created: ").append(offer.getId()).append(" / ").append(offer.getName()).append("\n");

        Demand demand = facade.createDemand(offer.getId(), representer.getId());
        log.append("Demand created: ").append(demand.getId()).append(" (").append(demand.getStatus()).append(")\n");

        Long rank = facade.getDemandRank(demand.getId());
        log.append("Demand rank: ").append(rank).append("\n");

        Demand approved = facade.validateOffer(representer.getId(), offer.getId());
        log.append("Approved demand: ").append(approved == null ? "null" : approved.getId()).append("\n");

        facade.archiveOffer(offer.getId());
        log.append("Offer archived\n");

        log.append("Stats (offers): ").append(facade.getOfferCountByAssociation()).append("\n");
        log.append("Stats (wins): ").append(facade.getOfferWinsByAssociation()).append("\n");
    }

    private void runErrorCases(StringBuilder log) {
        log.append("\n>> Testing error cases\n");

        try {
            facade.addMember(-1L, "BadMember");
        } catch (Exception e) {
            log.append("Expected error (addMember): ").append(e.getMessage()).append("\n");
        }

        try {
            facade.createOffer(-1L, "", "", BigDecimal.ZERO, List.of());
        } catch (Exception e) {
            log.append("Expected error (createOffer): ").append(e.getMessage()).append("\n");
        }

        try {
            facade.validateOffer(999L, 999L);
        } catch (Exception e) {
            log.append("Expected error (validateOffer): ").append(e.getMessage()).append("\n");
        }
    }
}

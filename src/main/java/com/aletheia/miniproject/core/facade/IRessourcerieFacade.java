package com.aletheia.miniproject.core.facade;

import com.aletheia.miniproject.core.entities.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface IRessourcerieFacade {
    Member createMember(String name, Association association);

    Category createCategory(String name);

    /**
     * Creates a new association and its contact person (representer).
     *
     * @param name            the name of the association
     * @return the created Association object, including its generated identifier
     */
    Association createAssociation(String name);

    /**
     * Adds a new member to an existing association.
     *
     * @param associationId the ID of the association that the member joins
     * @param memberName    the name of the new member
     * @return the created Member object, with its generated identifier
     */
    Member addMember(Long associationId, String memberName);

    /**
     * Creates a new offer on behalf of an association.
     * Only the association's representer is allowed to perform this action.
     *
     * @param contactId   the ID of the member who creates the offer
     * @param name        short name/title of the offer
     * @param description detailed description of the material offered
     * @param price       estimated price or value (may be zero)
     * @param categoryIds list of category IDs in which the offer should appear
     * @return the created Offer object, with status OPEN and a creation timestamp
     */
    Offer createOffer(Long contactId, String name, String description, BigDecimal price, List<Long> categoryIds);

    /**
     * Returns the list of all offers, regardless of category.
     *
     * @return a list containing all existing offers (OPEN or CLOSED but not archived)
     */
    List<Offer> listOffers();

    /**
     * Returns the list of offers that belong to a specific category.
     *
     * @param categoryId ID of the category; if null, all offers should be returned
     * @return the list of matching Offer objects
     */
    List<Offer> listOffersByCategory(Long categoryId);

    /**
     * Creates a new demand (request) for an offer.
     * Demands are ordered by creation date: first come, first served.
     *
     * @param offerId  the ID of the offer that the member wants
     * @param memberId the ID of the member making the demand
     * @return the created Demand object, with status PENDING and a timestamp
     */
    Demand createDemand(Long offerId, Long memberId);

    /**
     * Cancels an existing demand.
     * The demand is not deleted but marked as CANCELLED for statistics.
     *
     * @param demandId the ID of the demand to cancel
     */
    void cancelDemand(Long demandId);

    /**
     * Computes the rank (position) of a demand in the queue for its offer.
     *
     * @param demandId the ID of the demand
     * @return the rank (1 = first), or -1 if the demand does not exist
     */
    Long getDemandRank(Long demandId);

    /**
     * Validates an offer transfer.
     * The representer of the association selects the oldest PENDING demand.
     * That demand is marked as WON, others are marked as LOST,
     * and the offer is marked as CLOSED.
     *
     * @param contactMemberId ID of the representer performing the validation
     * @param offerId         ID of the offer to validate
     * @return the winning Demand, or null if no PENDING demand exists
     */
    Demand validateOffer(Long contactMemberId, Long offerId);

    /**
     * Archives an offer and all of its demands.
     * Archived objects are excluded from normal listings but kept for statistics.
     *
     * @param offerId ID of the offer to archive
     */
    void archiveOffer(Long offerId);

    /**
     * Computes how many offers each association has created.
     *
     * @return a map where the key is the association ID,
     * and the value is the number of offers it created
     */
    Map<Integer, Integer> getOfferCountByAssociation();

    /**
     * Computes how many offers each association has won.
     * An offer is won by the association of the member whose demand is marked as WON.
     *
     * @return a map where the key is the association ID,
     * and the value is the number of won offers
     */
    Map<Integer, Integer> getOfferWinsByAssociation();
}

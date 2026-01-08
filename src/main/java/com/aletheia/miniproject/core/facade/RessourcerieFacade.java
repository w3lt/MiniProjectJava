package com.aletheia.miniproject.core.facade;

import com.aletheia.miniproject.core.entities.*;
import com.aletheia.miniproject.repos.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RessourcerieFacade implements IRessourcerieFacade {
    private final IAssociationRepository associationRepo;
    private final ICategoryRepository categoryRepo;
    private final IOfferRepository offerRepo;
    private final IMemberRepository memberRepo;
    private final IDemandRepository demandRepo;

    public RessourcerieFacade(
            IAssociationRepository associationRepo,
            ICategoryRepository categoryRepo,
            IOfferRepository offerRepo,
            IMemberRepository memberRepo,
            IDemandRepository demandRepo
    ) {
        this.associationRepo = associationRepo;
        this.categoryRepo = categoryRepo;
        this.offerRepo = offerRepo;
        this.memberRepo = memberRepo;
        this.demandRepo = demandRepo;
    }

    @Override
    public Member createMember(String name, Association association) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Member name is null or blank");
        }
        Member member = new Member(name.trim(), association);
        return memberRepo.save(member);
    }

    @Override
    public Category createCategory(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name is null or blank");
        }
        Category category = new Category(name.trim());
        return categoryRepo.save(category);
    }

    /**
     * Create an association with an existing member as representer.
     * Nominal: name non-blank, representer exists.
     * Errors: IllegalArgumentException / IllegalStateException.
     */
    @Override
    public Association createAssociation(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Association name is null or blank");
        }

        Association association = new Association(name.trim());
        return associationRepo.save(association);
    }

    /**
     * Add a member to an association.
     * Nominal: association exists, memberName non-blank.
     */
    @Override
    public Member addMember(Long associationId, String memberName) {
        if (associationId <= 0) {
            throw new IllegalArgumentException("associationId is invalid: " + associationId);
        }

        if (memberName == null || memberName.isBlank()) {
            throw new IllegalArgumentException("Member name is null or blank");
        }

        Association association = associationRepo.findById(associationId)
                .orElseThrow(() -> new IllegalStateException("Association id not found: " + associationId));

        Member member = new Member(memberName.trim(), association);
        return memberRepo.save(member);
    }

    /**
     * Create an offer posted by an existing contact member.
     */
    @Override
    public Offer createOffer(Long contactId, String name, String description, BigDecimal price, List<Long> categoryIds) {
        if (contactId <= 0) throw new IllegalArgumentException("contactId must be > 0");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Offer name is null or blank");
        if (price == null) throw new IllegalArgumentException("Offer price is required");
        if (price.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Offer price cannot be negative");
        if (categoryIds == null || categoryIds.isEmpty()) throw new IllegalArgumentException("categoryIds is required");

        Member contact = memberRepo.findById(contactId)
                .orElseThrow(() -> new IllegalStateException("Contact member not found: " + contactId));

        if (contact.getAssociation() == null) {
            throw new IllegalStateException("Contact member has no association");
        }

        // Remove duplicates + nulls
        List<Long> distinctIds = categoryIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<Category> categories = categoryRepo.findAllById(distinctIds);

        if (categories.size() != distinctIds.size()) {
            Set<Long> found = categories.stream().map(Category::getId).collect(Collectors.toSet());
            List<Long> missing = distinctIds.stream().filter(id -> !found.contains(id)).toList();
            throw new IllegalStateException("Some categories not found: " + missing);
        }

        Offer offer = new Offer(
                contact.getAssociation(),
                name.trim(),
                description.trim(),
                price,
                LocalDateTime.now(),
                OfferStatus.OPEN // adjust if your enum uses another default
        );

        // Persist offer first (safer for join table + composite key setups)
        offer = offerRepo.save(offer);

        // Add categories using the join entity
        for (Category c : categories) {
            offer.addCategory(c);
        }

        return offerRepo.save(offer);
    }

    /**
     * List all offers.
     */
    @Override
    public List<Offer> listOffers() {
        return offerRepo.findAll();
    }

    /**
     * List offers by category.
     */
    @Override
    public List<Offer> listOffersByCategory(Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            throw new IllegalArgumentException("categoryId is invalid: " + categoryId);
        }

        return offerRepo.findDistinctByCategoriesId(categoryId);
    }

    /**
     * Create a demand for an offer by a member.
     */
    @Override
    public Demand createDemand(Long offerId, Long memberId) {
        if (offerId == null || offerId <= 0) {
            throw new IllegalArgumentException("offerId is invalid: " + offerId);
        }
        if (memberId == null || memberId <= 0) {
            throw new IllegalArgumentException("demanderId is invalid: " + memberId);
        }

        Offer offer = offerRepo.findById(offerId)
                .orElseThrow(() -> new IllegalStateException("Offer not found: " + offerId));

        if (offer.getStatus() != OfferStatus.OPEN) {
            throw new IllegalStateException("Offer is not OPEN (status=" + offer.getStatus() + ")");
        }

        Member demander = memberRepo.findById(memberId)
                .orElseThrow(() -> new IllegalStateException("Member not found: " + memberId));

        // Avoid duplicates (same demander can't demand same offer twice while still pending)
        boolean alreadyPending = demandRepo.existsPendingByOfferIdAndDemanderId(
                offerId,
                memberId
        );
        if (alreadyPending) {
            throw new IllegalStateException("This member already has a PENDING demand for this offer");
        }

        Demand demand = new Demand(
                offer,
                demander,
                LocalDateTime.now(),
                DemandStatus.PENDING
        );

        return demandRepo.save(demand);
    }

    @Override
    public void cancelDemand(Long demandId) {
        if (demandId == null || demandId <= 0) {
            throw new IllegalArgumentException("demandId is invalid: " + demandId);
        }

        Demand demand = demandRepo.findById(demandId)
                .orElseThrow(() -> new IllegalStateException("Demand not found: " + demandId));

        if (demand.getStatus() != DemandStatus.PENDING) {
            throw new IllegalStateException("Only PENDING demands can be cancelled");
        }

        demand.setStatus(DemandStatus.CANCELLED);
        demandRepo.save(demand);
    }

    @Override
    public Long getDemandRank(Long demandId) {
        if (demandId == null || demandId <= 0) {
            throw new IllegalArgumentException("demandId is invalid: " + demandId);
        }

        Demand demand = demandRepo.findById(demandId)
                .orElseThrow(() -> new IllegalStateException("Demand not found: " + demandId));

        Offer offer = demand.getOffer();
        if (offer == null) {
            throw new IllegalStateException("Demand has no offer");
        }

        List<Demand> demands = demandRepo.findByOfferIdOrderByCreatedAtAsc(offer.getId())
                .stream()
                .filter(d -> d.getStatus() == DemandStatus.PENDING)
                .toList();

        for (int i = 0; i < demands.size(); i++) {
            if (Objects.equals(demands.get(i).getId(), demandId)) {
                return (long) (i + 1);
            }
        }

        return null;
    }

    @Override
    public Demand validateOffer(Long contactMemberId, Long offerId) {
        if (contactMemberId == null || contactMemberId <= 0) {
            throw new IllegalArgumentException("contactMemberId is invalid");
        }
        if (offerId == null || offerId <= 0) {
            throw new IllegalArgumentException("offerId is invalid");
        }

        Member contact = memberRepo.findById(contactMemberId)
                .orElseThrow(() -> new IllegalStateException("Contact member not found: " + contactMemberId));

        Offer offer = offerRepo.findById(offerId)
                .orElseThrow(() -> new IllegalStateException("Offer not found: " + offerId));

        if (offer.getStatus() != OfferStatus.OPEN) {
            throw new IllegalStateException("Offer is not OPEN");
        }

        if (offer.getAssociation() == null ||
                contact.getAssociation() == null ||
                !offer.getAssociation().getId().equals(contact.getAssociation().getId())) {
            throw new IllegalStateException("Contact member is not allowed to validate this offer");
        }

        List<Demand> demands = demandRepo.findByOfferIdOrderByCreatedAtAsc(offerId);

        Demand approved = null;
        for (Demand d : demands) {
            if (d.getStatus() == DemandStatus.PENDING && approved == null) {
                d.setStatus(DemandStatus.APPROVED);
                approved = d;
            } else if (d.getStatus() == DemandStatus.PENDING) {
                d.setStatus(DemandStatus.REJECTED);
            }
            demandRepo.save(d);
        }

        offer.setStatus(OfferStatus.CLOSED);
        offer.setClosedAt(LocalDateTime.now());
        offerRepo.save(offer);

        return approved;
    }

    @Override
    public void archiveOffer(Long offerId) {
        if (offerId == null || offerId <= 0) {
            throw new IllegalArgumentException("offerId is invalid: " + offerId);
        }

        Offer offer = offerRepo.findById(offerId)
                .orElseThrow(() -> new IllegalStateException("Offer not found: " + offerId));

        offer.setStatus(OfferStatus.ARCHIVED);
        offer.setClosedAt(LocalDateTime.now());
        offerRepo.save(offer);
    }

    @Override
    public Map<Integer, Integer> getOfferCountByAssociation() {
        return offerRepo.findAll().stream()
                .filter(o -> o.getAssociation() != null && o.getAssociation().getId() != null)
                .collect(Collectors.groupingBy(
                        o -> o.getAssociation().getId().intValue(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }

    @Override
    public Map<Integer, Integer> getOfferWinsByAssociation() {
        Map<Integer, Integer> result = new HashMap<>();

        List<Demand> approvedDemands = demandRepo.findAll().stream()
                .filter(d -> d.getStatus() == DemandStatus.APPROVED)
                .toList();

        for (Demand d : approvedDemands) {
            Member m = d.getDemander();
            if (m == null || m.getAssociation() == null) continue;

            Integer associationId = m.getAssociation().getId().intValue();
            result.merge(associationId, 1, Integer::sum);
        }

        return result;
    }
}

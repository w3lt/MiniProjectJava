package core.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "offers_categories")
public class OfferCategory {
    @EmbeddedId
    private OfferCategoryKey id;

    @ManyToOne
    @MapsId("offerId")
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    @ManyToOne
    @MapsId("categoryId")
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    public OfferCategory() {
    }

    public OfferCategory(Offer offer, Category category) {
        this.offer = offer;
        this.category = category;
        this.id = new OfferCategoryKey(offer.getId(), category.getId());
    }

    public OfferCategoryKey getId() {
        return id;
    }

    public Offer getOffer() {
        return offer;
    }

    public void setOffer(Offer offer) {
        this.offer = offer;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}

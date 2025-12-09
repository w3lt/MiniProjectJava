package core.entities;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class OfferCategoryKey implements Serializable {
    @Column(name = "offer_id")
    private Long offerId;

    @Column(name = "category_id")
    private Long categoryId;

    public OfferCategoryKey() {
    }

    public OfferCategoryKey(Long offerId, Long categoryId) {
        this.offerId = offerId;
        this.categoryId = categoryId;
    }

    public Long getOfferId() {
        return offerId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setOfferId(Long offerId) {
        this.offerId = offerId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    // Required for composite keys:
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OfferCategoryKey that)) return false;
        return Objects.equals(offerId, that.offerId) && Objects.equals(categoryId, that.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offerId, categoryId);
    }
}

package com.aletheia.miniproject.core.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "offers")
public class Offer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "association_id", nullable = false)
    private Association association;

    @Column(nullable = false, length = 64)
    private String name;

    @Lob
    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime closedAt;

    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OfferCategory> categories = new HashSet<>();

    public Offer() {
    }

    public Offer(Association association, String name, String description, BigDecimal price, LocalDateTime createdAt, OfferStatus status) {
        this.association = association;
        this.name = name;
        this.description = description;
        this.price = price;
        this.createdAt = createdAt;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Association getAssociation() {
        return association;
    }

    public void setAssociation(Association association) {
        this.association = association;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public OfferStatus getStatus() {
        return status;
    }

    public void setStatus(OfferStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public Set<OfferCategory> getCategoryLinks() {
        return categories;
    }

    public Set<Category> getCategories() {
        return categories.stream().map(OfferCategory::getCategory).collect(Collectors.toSet());
    }

    public void addCategory(Category category) {
        if (category == null) throw new IllegalArgumentException("category is null");
        categories.add(new OfferCategory(this, category));
    }

    public void removeCategory(Category category) {
        if (category == null) return;
        categories.removeIf(oc -> oc.getCategory() != null && oc.getCategory().getId().equals(category.getId()));
    }

    public void clearCategories() {
        categories.clear();
    }
}

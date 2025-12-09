package core.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "demands")
public class Demand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demander_id", nullable = false)
    private Member demander;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DemandStatus status;

    public Demand() {
    }

    public Demand(Offer offer, Member demander, LocalDateTime createdAt, DemandStatus status) {
        this.offer = offer;
        this.demander = demander;
        this.createdAt = createdAt;
        this.status = status;
    }

    public Offer getOffer() {
        return offer;
    }

    public void setOffer(Offer offer) {
        this.offer = offer;
    }

    public Member getDemander() {
        return demander;
    }

    public void setDemander(Member demander) {
        this.demander = demander;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public DemandStatus getStatus() {
        return status;
    }

    public void setStatus(DemandStatus status) {
        this.status = status;
    }
}

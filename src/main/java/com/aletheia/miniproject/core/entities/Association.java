package com.aletheia.miniproject.core.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "associations")
public class Association {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String name;

    @OneToOne
    @JoinColumn(name = "representer_id", nullable = true)
    private Member representer;

    public Association() {
    }

    public Association(String name) {
        this.name = name;
    }

    public Association(String name, Member representer) {
        this.name = name;
        this.representer = representer;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Member getRepresenter() {
        return representer;
    }

    public void setRepresenter(Member representer) {
        this.representer = representer;
    }
}

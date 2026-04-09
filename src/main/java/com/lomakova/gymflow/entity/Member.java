package com.lomakova.gymflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(name = "visits_left")
    private int visitsLeft;

    @Column(name = "max_visits")
    private int maxVisits;

    @Transient
    private boolean excursedAbsence = false;

    public void decrementVisit(boolean allPresent) {
        if (allPresent || !excursedAbsence) {
            if (visitsLeft > 0) {
                visitsLeft--;
            }
        }
        this.excursedAbsence = false;
    }

    @Override
    public String toString() {
        return name + " (ID: " + id + ")";
    }
}
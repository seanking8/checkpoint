package com.checkpoint.model;

import jakarta.persistence.*;

@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String title;

    @Column(name = "cover_art_url")
    private String coverArtUrl;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;
}
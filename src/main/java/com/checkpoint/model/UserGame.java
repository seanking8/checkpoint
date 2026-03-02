package com.checkpoint.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "user_games",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"user_id", "game_id", "platform_id"}
                )
        }
)
public class UserGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne(optional = false)
    @JoinColumn(name = "platform_id")
    private Platform platform;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status = GameStatus.WANT_TO_PLAY;
}

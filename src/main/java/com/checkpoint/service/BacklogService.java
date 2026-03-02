package com.checkpoint.service;

import com.checkpoint.model.Game;
import com.checkpoint.model.Platform;
import com.checkpoint.model.User;
import com.checkpoint.model.UserGame;
import com.checkpoint.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class BacklogService {

    private final UserGameRepository userGameRepo;
    private final GameRepository gameRepo;
    private final PlatformRepository platformRepo;
    private final UserRepository userRepo;

    public BacklogService(
            UserGameRepository userGameRepo,
            GameRepository gameRepo,
            PlatformRepository platformRepo,
            UserRepository userRepo
    ) {
        this.userGameRepo = userGameRepo;
        this.gameRepo = gameRepo;
        this.platformRepo = platformRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public void addToBacklog(Long userId, Long gameId, Long platformId) {

        // Check if the user already has this game on this platform in their backlog
        if (userGameRepo.existsByUserIdAndGameIdAndPlatformId(
                userId, gameId, platformId)) {
            throw new IllegalStateException("Already in backlog");
        }

        Game game = gameRepo.findById(gameId).orElseThrow();
        Platform platform = platformRepo.findById(platformId).orElseThrow();

        if (!game.getPlatforms().contains(platform)) {
            throw new IllegalArgumentException("Game not available on platform");
        }

        User user = userRepo.findById(userId).orElseThrow();

        UserGame userGame = new UserGame();
        userGame.setUser(user);
        userGame.setGame(game);
        userGame.setPlatform(platform);

        userGameRepo.save(userGame);
    }
}

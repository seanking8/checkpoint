package com.checkpoint.service;

import com.checkpoint.dto.BacklogItemDto;
import com.checkpoint.error.DomainException;
import com.checkpoint.error.ErrorCode;
import com.checkpoint.model.Game;
import com.checkpoint.model.GameStatus;
import com.checkpoint.model.Platform;
import com.checkpoint.model.User;
import com.checkpoint.model.UserGame;
import com.checkpoint.repository.*;
import com.checkpoint.validation.BacklogDomainValidator;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BacklogService {

    private final UserGameRepository userGameRepo;
    private final GameRepository gameRepo;
    private final PlatformRepository platformRepo;
    private final UserRepository userRepo;
    private final BacklogDomainValidator backlogDomainValidator;

    public BacklogService(
            UserGameRepository userGameRepo,
            GameRepository gameRepo,
            PlatformRepository platformRepo,
            UserRepository userRepo,
            BacklogDomainValidator backlogDomainValidator
    ) {
        this.userGameRepo = userGameRepo;
        this.gameRepo = gameRepo;
        this.platformRepo = platformRepo;
        this.userRepo = userRepo;
        this.backlogDomainValidator = backlogDomainValidator;
    }

    public List<BacklogItemDto> listBacklogForUser(Long userId) {
        return userGameRepo.findBacklogItemsByUserId(userId);
    }

    @Transactional
    public boolean updateStatus(Long userId, Long backlogId, GameStatus status) {
        return userGameRepo.updateStatusByIdAndUserId(backlogId, userId, status) > 0;
    }

    @Transactional
    public boolean removeFromBacklog(Long userId, Long backlogId) {
        return userGameRepo.deleteByIdAndUserId(backlogId, userId) > 0;
    }

    @Transactional
    public void addToBacklog(Long userId, Long gameId, Long platformId) {

        Game game = gameRepo.findById(gameId)
                .orElseThrow(() -> new DomainException(ErrorCode.GAME_NOT_FOUND));
        Platform platform = platformRepo.findById(platformId)
                .orElseThrow(() -> new DomainException(ErrorCode.PLATFORM_NOT_FOUND));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new DomainException(ErrorCode.USER_NOT_FOUND));

        backlogDomainValidator.assertGameAvailableOnPlatform(game, platform);

        // Check if the user already has this game on this platform in their backlog
        backlogDomainValidator.assertNotAlreadyInBacklog(
                userGameRepo.existsByUserIdAndGameIdAndPlatformId(userId, gameId, platformId)
        );

        UserGame userGame = new UserGame();
        userGame.setUser(user);
        userGame.setGame(game);
        userGame.setPlatform(platform);

        userGameRepo.save(userGame);
    }
}

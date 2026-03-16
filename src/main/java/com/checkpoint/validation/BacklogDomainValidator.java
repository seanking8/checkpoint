package com.checkpoint.validation;

import com.checkpoint.error.DomainException;
import com.checkpoint.error.ErrorCode;
import com.checkpoint.model.Game;
import com.checkpoint.model.Platform;
import org.springframework.stereotype.Component;

@Component
public class BacklogDomainValidator {

    public void assertGameAvailableOnPlatform(Game game, Platform platform) {
        if (!game.getPlatforms().contains(platform)) {
            throw new DomainException(ErrorCode.GAME_NOT_AVAILABLE_ON_PLATFORM);
        }
    }

    public void assertNotAlreadyInBacklog(boolean alreadyExists) {
        if (alreadyExists) {
            throw new DomainException(ErrorCode.GAME_ALREADY_IN_BACKLOG);
        }
    }
}


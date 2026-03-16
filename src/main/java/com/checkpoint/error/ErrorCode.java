package com.checkpoint.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    USERNAME_TAKEN(HttpStatus.CONFLICT, "Username already taken"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid username or password"),
    GAME_ALREADY_IN_BACKLOG(HttpStatus.CONFLICT, "That game/platform is already in your backlog"),
    GAME_NOT_AVAILABLE_ON_PLATFORM(HttpStatus.BAD_REQUEST, "Game is not available on the selected platform"),
    GAME_NOT_FOUND(HttpStatus.NOT_FOUND, "Game not found"),
    PLATFORM_NOT_FOUND(HttpStatus.NOT_FOUND, "Platform not found"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    AT_LEAST_ONE_PLATFORM_REQUIRED(HttpStatus.BAD_REQUEST, "At least one platform is required"),
    INVALID_PLATFORM_SELECTION(HttpStatus.BAD_REQUEST, "One or more selected platforms are invalid"),
    GAME_TITLE_EXISTS(HttpStatus.CONFLICT, "Game title already exists");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}


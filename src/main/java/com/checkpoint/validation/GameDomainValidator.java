package com.checkpoint.validation;

import com.checkpoint.error.DomainException;
import com.checkpoint.error.ErrorCode;
import com.checkpoint.model.Platform;
import com.checkpoint.repository.PlatformRepository;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class GameDomainValidator {

    private final PlatformRepository platformRepository;

    public GameDomainValidator(PlatformRepository platformRepository) {
        this.platformRepository = platformRepository;
    }

    public Set<Platform> resolvePlatforms(List<Long> platformIds, boolean required) {
        if (platformIds == null) {
            if (required) {
                throw new DomainException(ErrorCode.AT_LEAST_ONE_PLATFORM_REQUIRED);
            }
            return Collections.emptySet();
        }

        Set<Long> requestedIds = platformIds.stream()
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());

        if (requestedIds.isEmpty()) {
            throw new DomainException(ErrorCode.AT_LEAST_ONE_PLATFORM_REQUIRED);
        }

        Set<Platform> selectedPlatforms = new HashSet<>();
        platformRepository.findAllById(requestedIds).forEach(selectedPlatforms::add);

        if (selectedPlatforms.size() != requestedIds.size()) {
            throw new DomainException(ErrorCode.INVALID_PLATFORM_SELECTION);
        }

        return selectedPlatforms;
    }
}


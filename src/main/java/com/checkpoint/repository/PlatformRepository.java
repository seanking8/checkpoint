package com.checkpoint.repository;

import com.checkpoint.model.Platform;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatformRepository extends CrudRepository<Platform, Long> {

}

package com.checkpoint.repository;

import com.checkpoint.model.UserGame;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserGameRepository extends CrudRepository<UserGame, Long> {

    List<UserGame> findByUserId(Long userId);

}

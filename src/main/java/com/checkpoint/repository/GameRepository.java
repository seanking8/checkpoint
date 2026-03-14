package com.checkpoint.repository;

import com.checkpoint.model.Game;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends CrudRepository<Game, Long> {

	@Query("""
			select distinct g
			from Game g
			left join fetch g.platforms p
			order by g.title asc
			""")
	List<Game> findAllWithPlatforms();

}

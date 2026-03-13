package com.checkpoint.repository;

import com.checkpoint.dto.BacklogItemDto;
import com.checkpoint.model.UserGame;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserGameRepository extends CrudRepository<UserGame, Long> {

    List<UserGame> findByUserId(Long userId);

    @Query("""
            select new com.checkpoint.dto.BacklogItemDto(
                ug.id,
                g.id,
                g.title,
                g.coverArtUrl,
                p.id,
                p.name,
                ug.status
            )
            from UserGame ug
            join ug.game g
            join ug.platform p
            where ug.user.id = :userId
            order by g.title asc, p.name asc
            """)
    List<BacklogItemDto> findBacklogItemsByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndGameIdAndPlatformId(
            Long userId, Long gameId, Long platformId
    );

}

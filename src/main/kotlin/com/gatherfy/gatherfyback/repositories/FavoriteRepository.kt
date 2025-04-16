package com.gatherfy.gatherfyback.repositories

import com.gatherfy.gatherfyback.entities.Favorite
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface FavoriteRepository : JpaRepository<Favorite, Long> {
    fun findFavoritesByUserId(userId: Long): List<Favorite>

    @Query("from favorites where userId = :userId and eventId = :eventId")
    fun findByUserIdAndAndEventId(@Param("userId")userId: Long, @Param("eventId") eventId: Long): Favorite?
}